/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.orm.PagedResultList
import grails.plugins.csv.CSVMapReader
import grails.validation.ValidationException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.hibernate.sql.JoinType
import org.pih.warehouse.allocation.AllocationRequest
import org.pih.warehouse.api.OutboundWorkflowState
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.AvailableItemStatus
import org.pih.warehouse.api.DocumentGroupCode
import org.pih.warehouse.api.PackPageItem
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.SubstitutionItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.StockMovementItemParamsCommand
import org.pih.warehouse.core.StockMovementItemsParamsCommand
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.ShipOrderCommand
import org.pih.warehouse.order.ShipOrderItemCommand
import org.pih.warehouse.picklist.PicklistImportDataCommand
import org.pih.warehouse.picklist.PicklistItemCommand
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.ReplenishmentTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionDataService
import org.pih.warehouse.requisition.RequisitionIdentifierService
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionStatusTransitionEvent
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentIdentifierService
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.shipping.ShipmentWorkflow
import org.pih.warehouse.PaginatedList
import org.springframework.web.multipart.MultipartFile

@Transactional
class StockMovementService {

    AuthService authService
    ProductService productService
    RequisitionIdentifierService requisitionIdentifierService
    ShipmentIdentifierService shipmentIdentifierService
    RequisitionService requisitionService
    ShipmentService shipmentService
    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    LocationService locationService
    DataService dataService
    ForecastingService forecastingService
    OutboundStockMovementService outboundStockMovementService
    UserService userService
    RequisitionDataService requisitionDataService
    GrailsApplication grailsApplication

    def createStockMovement(StockMovement stockMovement) {
        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }

        return createRequisitionBasedStockMovement(stockMovement)
    }

    void transitionStockMovement(StockMovement stockMovement, JSONObject jsonObject) {
        if (stockMovement.requisition) {
            transitionRequisitionBasedStockMovement(stockMovement, jsonObject)
        }
        else {
            transitionShipmentBasedStockMovement(stockMovement, jsonObject)
        }
    }

    void transitionShipmentBasedStockMovement(StockMovement stockMovement, JSONObject jsonObject) {
        RequisitionStatus status =
                jsonObject.containsKey("status") ? jsonObject.status as RequisitionStatus : null
        if (status == RequisitionStatus.ISSUED) {
            issueShipmentBasedStockMovement(stockMovement.id)
        }
        else {
            throw new UnsupportedOperationException("Updating inbound status not yet supported")
        }
    }

    void transitionRequisitionBasedStockMovement(StockMovement stockMovement, JSONObject jsonObject) {
        StockMovementStatusCode status =
                jsonObject.containsKey("status") ? jsonObject.status as StockMovementStatusCode : null

        Boolean statusOnly =
                jsonObject.containsKey("statusOnly") ? jsonObject.getBoolean("statusOnly") : false

        Comment comment = null
        // Update status only
        if (status && statusOnly) {
            RequisitionStatus requisitionStatus = RequisitionStatus.fromStockMovementStatus(stockMovementStatus)
            updateRequisitionStatus(stockMovement.id, requisitionStatus, comment)
        }
        // Determine whether we need to rollback change,
        else {

            // Determine whether we need to rollback changes
            Boolean rollback =
                    jsonObject.containsKey("rollback") ? jsonObject.getBoolean("rollback") : false

            if (rollback) {
                rollbackStockMovement(stockMovement.id)
            }

            if (status) {
                switch (status) {
                    //RequisitionStatus.CREATED:
                    case StockMovementStatusCode.CREATED:
                        break
                    //RequisitionStatus.EDITING:
                    case StockMovementStatusCode.REQUESTED:
                        break
                    //RequisitionStatus.PENDING_APPROVAL:
                    case StockMovementStatusCode.PENDING_APPROVAL:
                        break
                    //RequisitionStatus.VERIFYING:
                    case StockMovementStatusCode.VALIDATED:
                        break
                    //RequisitionStatus.APPROVED:
                    case StockMovementStatusCode.APPROVED:
                        if (!stockMovement.pendingApproval) {
                            throw new IllegalArgumentException("Cannot update to status ${jsonObject.status} because request is not pending approval")
                        }
                        break
                    //RequisitionStatus.REJECTED:
                    case StockMovementStatusCode.REJECTED:
                        if (!stockMovement.pendingApproval) {
                            throw new IllegalArgumentException("Cannot update to status ${jsonObject.status} because request is not pending approval")
                        }
                        comment = new Comment(jsonObject)
                        if (!comment.comment) {
                            throw new IllegalArgumentException("Comment is required before rejecting a request")
                        }
                        break
                    // RequisitionStatus.PICKING:
                    case StockMovementStatusCode.PICKING:
                        validateQuantityRequested(stockMovement)

                        // Clear picklist
                        Boolean shouldClearPicklist = jsonObject.containsKey("clearPicklist") ?
                                jsonObject.getBoolean("clearPicklist") : Boolean.FALSE

                        if (shouldClearPicklist) {
                            clearPicklist(stockMovement)
                        }

                        // Create picklist
                        Boolean shouldCreatePicklist = jsonObject.containsKey("createPicklist") ?
                                jsonObject.getBoolean("createPicklist") : Boolean.FALSE

                        if (shouldCreatePicklist) {
                            createPicklist(stockMovement)
                        }

                        break
                    case StockMovementStatusCode.PICKED:
                    case StockMovementStatusCode.PACKED:
                    case StockMovementStatusCode.CHECKING:
                    case StockMovementStatusCode.CHECKED:
                        def shipment = createShipment(stockMovement)
                        if (stockMovement?.requisition?.picklist) {
                            shipmentService.validateShipment(shipment)
                        }
                        break
                    case StockMovementStatusCode.DISPATCHED:
                        issueRequisitionBasedStockMovement(stockMovement.id)
                        break
                    default:
                        throw new IllegalArgumentException("Cannot update status with invalid status ${jsonObject.status}")
                        break

                }
                // If the dependent actions were updated properly then we can update the
                RequisitionStatus requisitionStatus = RequisitionStatus.fromStockMovementStatus(status)
                updateRequisitionStatus(stockMovement.id, requisitionStatus, comment)
            }
        }
    }

    void validateQuantityRequested(StockMovement stockMovement) {
        stockMovement.lineItems?.each { StockMovementItem item ->
            if (item.substitutionItems) {
                item.substitutionItems.each {
                    if (!validateQuantityRequested(it)) {
                        String errorMessage = "Product " + it.product?.productCode + " has no available inventory. Please go back to edit page and revise quantity"
                        stockMovement.errors.reject("stockMovement.invalidQtyRequested.message", [it.product?.name] as Object[], errorMessage)
                    }
                }
            } else {
                if (!validateQuantityRequested(item)) {
                    String errorMessage = "Product " + item.product?.productCode + " has no available inventory. Please go back to edit page and revise quantity"
                    stockMovement.errors.reject("stockMovement.invalidQtyRequested.message", [item.product?.name] as Object[], errorMessage)
                }
            }
        }

        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }
    }

    Boolean validateQuantityRequested(StockMovementItem item) {
        RequisitionItem requisitionItem = item.requisitionItem
        Location location = requisitionItem?.requisition?.origin
        Integer quantityRequired = requisitionItem?.calculateQuantityRequired()

        if (requisitionItem.modificationItem) {
            requisitionItem = requisitionItem.modificationItem
        }

        //When picklist is created it will change the quantity ATP, so we need to add quantity picked to the quantity ATP,
        //because the quantity required was already picked
        def quantityAvailable = inventoryService.getQuantityAvailableToPromise(item.product, location)
        def qtyPicked = requisitionItem.pickablePicklistItems?.sum { it.quantity }
        quantityAvailable += (qtyPicked ?: 0)

        if (quantityRequired > quantityAvailable) {
            return false
        }

        return true
    }

    void updateRequisitionStatus(String id, RequisitionStatus status, Comment comment = null) {

        log.info "Update status ${id} " + status
        Requisition requisition = requisitionDataService.getRequisitionWithEvents(id)
        if (status == RequisitionStatus.CHECKING) {
            Shipment shipment = requisition.shipment
            shipment?.expectedShippingDate = new Date()
        }

        if (!(status in RequisitionStatus.list())) {
            throw new IllegalStateException("Transition from ${requisition.status.name()} to ${status.name()} is not allowed")
        } else if (status < requisition.status) {
            // Ignore backwards state transitions since it occurs normally when users go back and edit pages earlier in the workflow
            log.warn("Transition from ${requisition.status.name()} to ${status.name()} is not allowed - use rollback instead")
        } else {
            requisitionService.triggerRequisitionStatusTransition(requisition, AuthService.currentUser, status, comment)
            grailsApplication.mainContext.publishEvent(new RequisitionStatusTransitionEvent(requisition))
        }
    }


    StockMovement updateStockMovement(StockMovement stockMovement) {
        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }

        if (stockMovement.requisition) {
            return updateRequisitionBasedStockMovement(stockMovement)
        } else {
            return updateShipmentBasedStockMovement(stockMovement)
        }
    }


    StockMovement updateShipmentBasedStockMovement(StockMovement stockMovement) {
        log.info "Update stock movement " + new JSONObject(stockMovement.toJson()).toString(4)

        Shipment shipment = Shipment.get(stockMovement.id)
        if (!shipment) {
            throw new ObjectNotFoundException(stockMovement.id, StockMovement.class.toString())
        }
        if (stockMovement.destination) shipment.destination = stockMovement.destination
        if (stockMovement.origin) shipment.origin = stockMovement.origin
        if (stockMovement.description) shipment.description = stockMovement.description
        if (stockMovement.requestedBy) shipment.createdBy = stockMovement.requestedBy
        if (stockMovement.dateRequested) shipment.dateCreated = stockMovement.dateRequested
        shipment.name = stockMovement.generateName()

        if (stockMovement?.stocklist?.id) {
            throw new UnsupportedOperationException("Stocklists not yet supported for inbound stock movements")
        }
        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        return StockMovement.createFromShipment(shipment)
    }


    StockMovement updateRequisitionBasedStockMovement(StockMovement stockMovement) {
        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            throw new ObjectNotFoundException(stockMovement.id, StockMovement.class.toString())
        }

        if (stockMovement.destination) requisition.destination = stockMovement.destination
        if (stockMovement.origin) requisition.origin = stockMovement.origin
        if (stockMovement.description) requisition.description = stockMovement.description
        if (stockMovement.requestedBy) requisition.requestedBy = stockMovement.requestedBy
        if (stockMovement.dateRequested) requisition.dateRequested = stockMovement.dateRequested
        if (stockMovement.requestType) requisition.type = stockMovement.requestType
        if (stockMovement.approvers != null) requisition.approvers = stockMovement.approvers

        requisition.dateDeliveryRequested = stockMovement.dateDeliveryRequested
        requisition.name = stockMovement.generateName()

        if (requisition.requisitionTemplate?.id != stockMovement.stocklist?.id) {
            removeRequisitionItems(requisition)
            addStockListItemsToRequisition(stockMovement, requisition)
            requisition.requisitionTemplate = stockMovement.stocklist
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        updateShipmentOnRequisitionChange(stockMovement)
        StockMovement savedStockMovement = StockMovement.createFromRequisition(requisition.refresh())

        createMissingPicklistItems(savedStockMovement)
        createMissingShipmentItems(savedStockMovement)

        return savedStockMovement
    }

    void updateRequisitionOnShipmentChange(StockMovement stockMovement) {
        log.info "Update stock movement " + new JSONObject(stockMovement.toJson()).toString(4)

        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            throw new ObjectNotFoundException(stockMovement.id, StockMovement.class.toString())
        }

        requisition.name = stockMovement.description == requisition.description && requisition.destination == stockMovement.destination ? stockMovement.name : stockMovement.generateName()
        requisition.destination = stockMovement.destination
        requisition.description = stockMovement.description

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }
    }

    void deleteStockMovement(String id) {
        StockMovement stockMovement = getStockMovement(id)
        deleteStockMovement(stockMovement)
    }

    // stockMovement could be an instance of StockMovement or OutboundStockMovement
    void deleteStockMovement(def stockMovement) {
        if (stockMovement?.requisition) {
            def shipments = stockMovement?.requisition?.shipments
            shipments.toArray().each { Shipment shipment ->
                stockMovement?.requisition.removeFromShipments(shipment)
                if (!shipment?.events?.empty) {
                    shipmentService.rollbackLastEvent(shipment)
                }
                shipmentService.deleteShipment(shipment)
            }
            Requisition requisition = stockMovement?.requisition
            stockMovement?.shipment = null
            stockMovement?.requisition = null
            requisitionService.deleteRequisition(requisition)
        }
        else {
            shipmentService.deleteShipment(stockMovement?.shipment)
        }
    }

    def getStockMovements(StockMovement criteria, Map params) {
        params.includeStockMovementItems = false
        switch(criteria.stockMovementDirection) {
            case StockMovementDirection.OUTBOUND:
                return outboundStockMovementService.getStockMovements(criteria, params)
            case StockMovementDirection.INBOUND:
                return getInboundStockMovements(criteria, params)
            default:
                throw new IllegalArgumentException("Missing stock movement direction parameter")
        }
    }

    def getInboundStockMovements(StockMovement criteria, Map params) {
        def max = params.max ? params.int("max") : null
        def offset = params.offset ? params.int("offset") : null
        Date createdAfter = params.createdAfter ? Date.parse("MM/dd/yyyy", params.createdAfter) : null
        Date createdBefore = params.createdBefore ? Date.parse("MM/dd/yyyy", params.createdBefore) : null
        List<ShipmentType> shipmentTypes = params.list("shipmentType") ? params.list("shipmentType").collect{ ShipmentType.read(it) } : null
        Location currentLocation = AuthService.currentLocation

        PagedResultList shipments = Shipment.createCriteria().list(max: max, offset: offset) {
            // OBPIH-6403: We want to hide SMs with requisition of status REJECTED from the inbound list (only for the depot-depot case!!)
            // The "or" is needed, because otherwise, SMs without requisition were also filtered out from the list (e.g. shipment from PO)
            if (!currentLocation?.downstreamConsumer) {
                or {
                    requisition(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                        ne("status", RequisitionStatus.REJECTED)
                    }
                    isNull("requisition")
                }
            }

            if (criteria?.identifier || criteria.name || criteria?.description) {
                or {
                    if (criteria?.identifier) {
                        ilike("shipmentNumber", criteria.identifier)
                    }
                    if (criteria?.name) {
                        ilike("name", criteria.name)
                    }
                    if (criteria?.description) {
                        ilike("description", criteria.description)
                    }
                }
            }
            if (criteria.destination) eq("destination", criteria.destination)
            if (criteria.origin) eq("origin", criteria.origin)
            if (criteria.receiptStatusCodes) 'in'("currentStatus", criteria.receiptStatusCodes)
            if (criteria.createdBy) {
                eq("createdBy", criteria?.createdBy)
            }
            if (criteria.requestedBy) {
                or {
                    requisition(JoinType.LEFT_OUTER_JOIN.joinTypeValue) {
                        eq("requestedBy", criteria?.requestedBy)
                    }
                    and {
                        isNull("requisition")
                        eq("createdBy", criteria?.requestedBy)
                    }
                }
            }
            if (criteria.updatedBy) {
                eq("updatedBy", criteria.updatedBy)
            }
            if(createdAfter) {
                ge("dateCreated", createdAfter)
            }
            if(createdBefore) {
                le("dateCreated", createdBefore)
            }
            if (shipmentTypes) {
                'in'("shipmentType", shipmentTypes)
            }

            if (params.sort) {
                if (params.sort == "destination.name") {
                    destination {
                        order("name", params.order ?: "desc")
                    }
                } else if (params.sort == "origin.name") {
                    origin {
                        order("name", params.order ?: "desc")
                    }
                } else if (params.sort == "dateRequested") {
                    requisition {
                        order("dateRequested", params.order ?: "desc")
                    }
                } else if (params.sort == "stocklist.name") {
                    requisition(JoinType.LEFT_OUTER_JOIN.joinTypeValue)  {
                        requisitionTemplate(JoinType.LEFT_OUTER_JOIN.joinTypeValue)  {
                            order("name", params.order ?: "desc")
                        }
                    }
                } else if (params.sort == "identifier") {
                    order("shipmentNumber", params.order ?: "desc")
                } else {
                    order(params.sort, params.order ?: "desc")
                }
            } else {
                order("dateCreated", "desc")
            }
        }
        List<StockMovement> stockMovements = shipments.collect { Shipment shipment ->
            if (shipment.requisition) {
                return StockMovement.createFromRequisition(shipment.requisition, params.includeStockMovementItems)
            } else {
                return StockMovement.createFromShipment(shipment, params.includeStockMovementItems)
            }
        }
        return new PaginatedList<StockMovement>(stockMovements, shipments.totalCount)
    }

    def getOutboundStockMovements(Integer maxResults, Integer offset) {
        return getOutboundStockMovements(new StockMovement(), [maxResults:maxResults, offset:offset])
    }

    def getOutboundStockMovements(StockMovement stockMovement, Map params) {
        log.info "Stock movement: ${stockMovement?.currentStatus}"

        def requisitions = Requisition.createCriteria().list(max: params.max, offset: params.offset) {
            eq("isTemplate", Boolean.FALSE)

            if (stockMovement?.receiptStatusCodes) {
                shipments {
                    'in'("currentStatus", stockMovement.receiptStatusCodes)
                }
            }

            if (stockMovement?.identifier || stockMovement.name || stockMovement?.description) {
                or {
                    if (stockMovement?.identifier) {
                        ilike("requestNumber", stockMovement.identifier)
                    }
                    if (stockMovement?.name) {
                        ilike("name", stockMovement.name)
                    }
                    if (stockMovement?.description) {
                        ilike("description", stockMovement.description)
                    }
                }
            }

            if (stockMovement.destination == stockMovement?.origin) {
                or {
                    if (stockMovement?.destination) {
                        eq("destination", stockMovement.destination)
                    }
                    if (stockMovement?.origin) {
                        eq("origin", stockMovement.origin)
                    }
                }
            } else {
                if (stockMovement?.destination) {
                    eq("destination", stockMovement.destination)
                }
                if (stockMovement?.origin) {
                    eq("origin", stockMovement.origin)
                }
            }
            if (stockMovement.requisitionStatusCodes) {
                'in'("status", stockMovement.requisitionStatusCodes)
            }
            if (stockMovement.requestedBy) {
                eq("requestedBy", stockMovement.requestedBy)
            }
            if (stockMovement.createdBy) {
                eq("createdBy", stockMovement.createdBy)
            }
            if (stockMovement.updatedBy) {
                eq("updatedBy", stockMovement.updatedBy)
            }
            if (stockMovement.requestType) {
                eq("type", stockMovement.requestType)
            }
            if (stockMovement.sourceType) {
                eq ('sourceType', stockMovement.sourceType)
                if (stockMovement.sourceType == RequisitionSourceType.ELECTRONIC) {
                    not {
                        'in'("status", [RequisitionStatus.CREATED, RequisitionStatus.ISSUED, RequisitionStatus.CANCELED])
                    }
                }
            }
            if(params.createdAfter) {
                ge("dateCreated", params.createdAfter)
            }
            if(params.createdBefore) {
                le("dateCreated", params.createdBefore)
            }
            if (params.sort && params.order) {
                order(params.sort, params.order)
            } else {
                order("statusSortOrder", "asc")
                order("dateCreated", "desc")
            }
        }

        def stockMovements = requisitions.collect { requisition ->
            return StockMovement.createFromRequisition(requisition, params.includeStockMovementItems)
        }

        return new PaginatedList<StockMovement>(stockMovements, requisitions.totalCount)
    }

    @Transactional(readOnly=true)
    StockMovement getStockMovement(String id) {
        return getStockMovement(id, (String) null)
    }

    @Transactional(readOnly=true)
    StockMovement getStockMovement(String id, String stepNumber) {
        Requisition requisition = Requisition.get(id)
        if (requisition) {
            return getRequisitionBasedStockMovement(requisition, stepNumber)
        } else {
            Shipment shipment = Shipment.get(id)
            if (shipment?.requisition) {
                log.info "Shipment.requisition ${shipment.requisition}"
                return getRequisitionBasedStockMovement(shipment.requisition, stepNumber)
            }
            else if (shipment) {
                log.info "Shipment ${shipment}"
                return getShipmentBasedStockMovement(shipment)
            }
            else {
                throw new ObjectNotFoundException(id, StockMovement.class.toString())
            }
        }
    }

    StockMovement getShipmentBasedStockMovement(Shipment shipment) {
        StockMovement stockMovement = StockMovement.createFromShipment(shipment)
        stockMovement.documents = getDocuments(stockMovement)
        return stockMovement
    }

    StockMovement getRequisitionBasedStockMovement(Requisition requisition, String stepNumber) {
        StockMovement stockMovement = StockMovement.createFromRequisition(requisition)
        stockMovement.documents = getDocuments(stockMovement)
        return stockMovement
    }

    @Transactional(readOnly=true)
    StockMovementItem getStockMovementItem(String id) {
        RequisitionItem requisitionItem = RequisitionItem.get(id)
        return StockMovementItem.createFromRequisitionItem(requisitionItem)
    }

    void removeStockMovementItem(String id) {
        RequisitionItem requisitionItem = RequisitionItem.get(id)
        if (requisitionItem) {
            removeRequisitionItem(requisitionItem)
            return
        }
        ShipmentItem shipmentItem = ShipmentItem.get(id)
        if (shipmentItem) {
            removeShipmentItem(shipmentItem)
        }
    }

    def getPendingRequisitionItems(Location origin) {
        def requisitionItems = RequisitionItem.createCriteria().list {
            and {
                gt("quantityApproved", 0)
                requisition {
                    and {
                        eq("origin", origin)
                        'in'("status", [RequisitionStatus.PICKED, RequisitionStatus.CHECKING])
                    }
                }
            }
        }
        return requisitionItems
    }

    def getPendingRequisitionDetails(Location origin, Product product, def currentRequisitionId) {
        def results = []
        def pendingRequisitionDetails = requisitionService.getPendingRequisitionItems(origin, product)
        pendingRequisitionDetails = pendingRequisitionDetails.findAll { it.requisition.id != currentRequisitionId }

        pendingRequisitionDetails.groupBy { it.requisition.destination }.collect { Location destination, List<RequisitionItem> requisitionItems ->
            def demandDetails = forecastingService.getDemand(origin, destination, product)
            if (destination.isDepot()) {
                def quantityOnHandAtDestination = productAvailabilityService.getQuantityOnHand(product, destination)
                demandDetails << [quantityOnHandAtDestination: quantityOnHandAtDestination]
            }

            def requisitionDetails = requisitionItems.groupBy { it.requisition }.collect { Requisition requisition, List<RequisitionItem> items ->
                [
                        'requisition.id'            : requisition.id,
                        'requisition.requestNumber' : requisition.requestNumber,
                        quantityRequested           : items.quantity.sum(),
                        quantityPicked              : items.sum() { RequisitionItem requisitionItem -> requisitionItem.calculateQuantityPicked() },
                ]
            }

            def details = requisitionDetails.pop()

            results << [
                    'destination.name'          : destination.name,
                    quantityOnHandAtDestination : demandDetails.quantityOnHandAtDestination,
                    averageMonthlyDemand        : demandDetails.monthlyDemand as Integer,
                    requisitions                : requisitionDetails,
                    'requisition.id'            : details['requisition.id'],
                    'requisition.requestNumber' : details['requisition.requestNumber'],
                    quantityRequested           : details.quantityRequested,
                    quantityPicked              : details.quantityPicked,
            ]
        }

        return results
    }

    def getStockMovementItem(StockMovementItemParamsCommand command) {
        getStockMovementItem(command.id, command.stepNumber, command.showDetails, command.refreshPicklistItems)
    }

    def getStockMovementItem(String id, Integer stepNumber, Boolean showDetails, Boolean refreshPicklistItems) {
        RequisitionItem requisitionItem = RequisitionItem.get(id)
        StockMovementItem stockMovementItem = null
        Requisition requisition = null
        ShipmentItem shipmentItem = null

        if (OutboundWorkflowState.fromStepNumber(stepNumber) == OutboundWorkflowState.REVISE_ITEMS) {
            return getEditPageItem(requisitionItem)
        }

        if (requisitionItem) {
            stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)
            requisition = requisitionItem.requisition
        } else {
            shipmentItem = ShipmentItem.get(id)

            if (shipmentItem) {
                stockMovementItem = StockMovementItem.createFromShipmentItem(shipmentItem)
                if (stockMovementItem.inventoryItem) {
                    def quantity = productAvailabilityService.getQuantityOnHand(stockMovementItem.inventoryItem)
                    stockMovementItem.inventoryItem.quantity = quantity
                }
            }
        }

        switch(OutboundWorkflowState.fromStepNumber(stepNumber)) {
            case OutboundWorkflowState.ADD_ITEMS:
                return getAddPageItem(requisition, stockMovementItem)
            case OutboundWorkflowState.PICK_ITEMS:
                if (refreshPicklistItems) {
                    allocatePicklistItems(requisition.requisitionItems?.asList())
                }
                return buildPickPageItem(requisitionItem, stockMovementItem.sortOrder, showDetails)
            case OutboundWorkflowState.PACK_ITEMS:
                return buildPackPageItem(shipmentItem)
            case OutboundWorkflowState.SEND_SHIPMENT:
                if (requisition && !requisition.origin.isSupplier() && requisition.origin.supports(ActivityCode.MANAGE_INVENTORY)) {
                    return buildPackPageItem(shipmentItem)
                }
            default:
                return stockMovementItem
        }
    }

    def getStockMovementItems(StockMovementItemsParamsCommand command) {
        getStockMovementItems(command.id, command.stepNumber, command.max, command.offset, command.refreshPicklistItems)
    }

    def getStockMovementItems(String id, Integer stepNumber, Integer max, Integer offset, Boolean refreshPicklistItems) {
        // FIXME should get stock movement instead of requisition
        Requisition requisition = Requisition.get(id)
        List<StockMovementItem> stockMovementItems = []

        if (OutboundWorkflowState.fromStepNumber(stepNumber) == OutboundWorkflowState.REVISE_ITEMS) {
            return getEditPageItems(requisition, max, offset)
        }

        if (requisition) {
            List <RequisitionItem> requisitionItems = []
            if (max != null && offset != null) {
                requisitionItems = RequisitionItem.createCriteria().list(max: max, offset: offset) {
                    eq("requisition", requisition)
                    isNull("parentRequisitionItem")
                    order("orderIndex", 'asc')
                }
            } else {
                requisitionItems = RequisitionItem.createCriteria().list() {
                    eq("requisition", requisition)
                    isNull("parentRequisitionItem")
                    order("orderIndex", 'asc')
                }
            }
            requisitionItems.each { requisitionItem ->
                StockMovementItem stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)
                InventoryItem inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(stockMovementItem.product, stockMovementItem.lotNumber)
                if (inventoryItem) {
                    inventoryItem.quantity = productAvailabilityService.getQuantityOnHand(inventoryItem)
                    stockMovementItem.inventoryItem = inventoryItem
                }
                stockMovementItems.add(stockMovementItem)
            }
        } else {
            Shipment shipment = Shipment.get(id)
            List <ShipmentItem> shipmentItems = []
            if (max != null && offset != null) {
                shipmentItems = ShipmentItem.createCriteria().list(max: max, offset: offset) {
                    eq("shipment", shipment)
                    order("sortOrder", 'asc')
                }
            } else {
                shipmentItems = ShipmentItem.createCriteria().list() {
                    eq("shipment", shipment)
                    order("sortOrder", 'asc')
                }
            }
            shipmentItems.each { shipmentItem ->
                StockMovementItem stockMovementItem = StockMovementItem.createFromShipmentItem(shipmentItem)
                if (stockMovementItem.inventoryItem) {
                    def quantity = productAvailabilityService.getQuantityOnHand(stockMovementItem.inventoryItem)
                    stockMovementItem.inventoryItem.quantity = quantity
                }
                stockMovementItems.add(stockMovementItem)
            }
        }

        switch(OutboundWorkflowState.fromStepNumber(stepNumber)) {
            case OutboundWorkflowState.ADD_ITEMS:
                return getAddPageItems(requisition, stockMovementItems)
            case OutboundWorkflowState.PICK_ITEMS:
                if (refreshPicklistItems) {
                    allocatePicklistItems(requisition.requisitionItems?.asList())
                }
                return getPickPageItems(id, max, offset)
            case OutboundWorkflowState.PACK_ITEMS:
                return getPackPageItems(id, max, offset)
            case OutboundWorkflowState.SEND_SHIPMENT:
                if (requisition && !requisition.origin.isSupplier() && requisition.origin.supports(ActivityCode.MANAGE_INVENTORY)) {
                    return getPackPageItems(id, max, offset)
                }
            default:
                return stockMovementItems
        }
    }

    def getAddPageItems(Requisition requisition, List stockMovementItems) {
        return stockMovementItems.collect {
            stockMovementItem -> getAddPageItem(requisition, stockMovementItem)
        }
    }

    def getAddPageItem(Requisition requisition, def stockMovementItem) {
        if (stockMovementItem && requisition && requisition.sourceType == RequisitionSourceType.ELECTRONIC) {
            def quantityOnHand = productAvailabilityService.getQuantityOnHand(stockMovementItem.product, requisition.destination)
            def quantityAvailable = inventoryService.getQuantityAvailableToPromise(stockMovementItem.product, requisition.destination)
            def template = requisition.requisitionTemplate
            if (requisition.requisitionTemplateId && requisition.type == RequisitionType.STOCK && requisition.destination.locationType.locationTypeCode == LocationTypeCode.WARD) {
                def demand = forecastingService.getDemand(requisition.origin, requisition.destination, stockMovementItem.product)
                return [
                        id                              : stockMovementItem.id,
                        version                         : stockMovementItem.version,
                        product                         : stockMovementItem.product,
                        productCode                     : stockMovementItem.productCode,
                        quantityOnHand                  : quantityOnHand ?: 0,
                        quantityAllowed                 : stockMovementItem.quantityAllowed,
                        quantityRequested               : stockMovementItem.quantityRequested,
                        quantityCounted                 : stockMovementItem.quantityCounted,
                        comments                        : stockMovementItem.comments,
                        statusCode                      : stockMovementItem.statusCode,
                        sortOrder                       : stockMovementItem.sortOrder,
                        monthlyDemand                   : demand?.monthlyDemand ?: 0,
                        demandPerReplenishmentPeriod    : Math.ceil((demand?.dailyDemand ?: 0) * (template?.replenishmentPeriod ?: 30)),
                        manuallyAdded                   : stockMovementItem.manuallyAdded,
                ]
            } else if (!template || (template && template.replenishmentTypeCode == ReplenishmentTypeCode.PULL)) {
                def demand
                // if request FROM downstream consumer (location without managed inventory but supporting submitting requests),
                // then pull demand from origin to that location
                if (requisition?.destination?.isDownstreamConsumer()) {
                    demand = forecastingService.getDemand(requisition.origin, requisition.destination, stockMovementItem.product)
                } else {
                    // if request is NOT FROM downstream consumer, then pull demand outgoing FROM destination to all other locations
                    demand = forecastingService.getDemand(requisition.destination, null, stockMovementItem.product)
                }
                return [
                        id                              : stockMovementItem.id,
                        version                         : stockMovementItem.version,
                        product                         : stockMovementItem.product,
                        productCode                     : stockMovementItem.productCode,
                        quantityOnHand                  : quantityOnHand ?: 0,
                        quantityCounted                 : stockMovementItem.quantityCounted,
                        quantityAvailable               : quantityAvailable ?: 0,
                        quantityAllowed                 : stockMovementItem.quantityAllowed,
                        comments                        : stockMovementItem.comments,
                        quantityRequested               : stockMovementItem.quantityRequested,
                        statusCode                      : stockMovementItem.statusCode,
                        sortOrder                       : stockMovementItem.sortOrder,
                        monthlyDemand                   : demand?.monthlyDemand ?: 0,
                        demandPerReplenishmentPeriod    : Math.ceil((demand?.dailyDemand ?: 0) * (template?.replenishmentPeriod ?: 30)),
                        manuallyAdded                   : stockMovementItem.manuallyAdded,
                ]
            } else {
                return [
                        id                  : stockMovementItem.id,
                        version             : stockMovementItem.version,
                        product             : stockMovementItem.product,
                        productCode         : stockMovementItem.productCode,
                        quantityOnHand      : quantityOnHand ?: 0,
                        quantityAvailable   : quantityAvailable ?: 0,
                        quantityAllowed     : stockMovementItem.quantityAllowed,
                        quantityCounted     : stockMovementItem.quantityCounted,
                        comments            : stockMovementItem.comments,
                        quantityRequested   : stockMovementItem.quantityRequested,
                        statusCode          : stockMovementItem.statusCode,
                        sortOrder           : stockMovementItem.sortOrder,
                        manuallyAdded       : stockMovementItem.manuallyAdded,
                ]
            }
        }

        return stockMovementItem
    }

    def getEditPageItem(String id) {
        RequisitionItem requisitionItem = RequisitionItem.get(id)

        return getEditPageItem(requisitionItem)
    }

    def getEditPageItem(RequisitionItem requisitionItem) {
        def query = """ select * FROM edit_page_item where id = :itemId """

        def data = dataService.executeQuery(query, ['itemId': requisitionItem?.id])

        List editPageItems = buildEditPageItems(data)
        def editPageItem = editPageItems ? editPageItems.first() : null

        if (editPageItem) {
            Requisition requisition = Requisition.get(requisitionItem?.requisition?.id)

            if (requisition && requisition.sourceType == RequisitionSourceType.ELECTRONIC) {
                def quantityOnHandRequestingMap = productAvailabilityService.getQuantityOnHand(editPageItems.collect { it.product }, requisition.destination)
                        .inject([:]) {map, item -> map << [(item.prod.id): item.quantityOnHand]}

                calculateFieldsForElectronicRequisitionItem(requisition, editPageItem, quantityOnHandRequestingMap)
            }
        }

        return editPageItem
    }

    List getEditPageItems(Requisition requisition, Integer max, Integer offset) {
        def query = offset ?
                """ select * FROM edit_page_item where requisition_id = :requisition and requisition_item_type = 'ORIGINAL' ORDER BY sort_order limit :offset, :max; """ :
                """ select * FROM edit_page_item where requisition_id = :requisition and requisition_item_type = 'ORIGINAL' ORDER BY sort_order """

        def data = dataService.executeQuery(query, [
                'requisition': requisition.id,
                'offset'     : offset,
                'max'        : max,
        ])

        List editPageItems = buildEditPageItems(data)

        if (requisition && requisition.sourceType == RequisitionSourceType.ELECTRONIC) {
            def quantityOnHandRequestingMap = productAvailabilityService.getQuantityOnHand(editPageItems.collect { it.product }, requisition.destination)
                    .inject([:]) {map, item -> map << [(item.prod.id): item.quantityOnHand]}

            editPageItems.each { editPageItem ->
                calculateFieldsForElectronicRequisitionItem(requisition, editPageItem, quantityOnHandRequestingMap)
            }
        }

        return editPageItems
    }

    void calculateFieldsForElectronicRequisitionItem(Requisition requisition, def editPageItem, def quantityOnHandRequestingMap) {
        // origin = fulfilling, destination = requesting
        editPageItem << [quantityOnHandRequesting: quantityOnHandRequestingMap[editPageItem.product.id]]

        def template = requisition.requisitionTemplate
        if (!template || (template && template.replenishmentTypeCode == ReplenishmentTypeCode.PULL)) {
            def quantityDemand = forecastingService.getDemand(requisition.destination, null, editPageItem.product)
            editPageItem << [
                    quantityDemandRequesting        : quantityDemand?.monthlyDemand?:0,
                    demandPerReplenishmentPeriod    : Math.ceil((quantityDemand?.dailyDemand?:0) * (template?.replenishmentPeriod?:30))
            ]
        } else {
            def stocklist = Requisition.get(requisition.requisitionTemplate.id)
            def quantityOnStocklist = 0
            RequisitionItemSortByCode sortByCode = requisition.requisitionTemplate?.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX
            stocklist."${sortByCode.methodName}"?.eachWithIndex { item, index ->
                if (item.product == editPageItem.product && (index * 100 == editPageItem.sortOrder || index == editPageItem.sortOrder)) {
                    quantityOnStocklist = item.quantity
                }
            }
            editPageItem << [quantityOnStocklist: quantityOnStocklist]
        }
    }

    List buildEditPageItems(def data) {
        def editItemsIds = data.collect { "'$it.id'" }.join(',')

        def substitutionItemsMap = dataService.executeQuery("""
                    select
                       *
                    FROM substitution_item
                    where parent_requisition_item_id in (${editItemsIds})
                    """).groupBy { it.parent_requisition_item_id }

        List<String> productsIds = data.collect { it.product_id }
        List<Product> products = Product.findAllByIdInList(productsIds)
        Map productsMap = products.inject([:]) { map, item -> map << [(item.id): item] }

        Requisition requisition = Requisition.get(data.first()?.requisition_id)
        def picklistItemsMap = requisition?.picklist?.pickablePicklistItemsByProductId

        Map<String, List<AvailableItem>> availableItemsMap = productAvailabilityService
                .getAllAvailableBinLocations(requisition.origin, productsIds)
                .groupBy { it?.inventoryItem?.product?.id }

        def editPageItems = data.collect {
            def substitutionItems = substitutionItemsMap[it.id]

            def statusCode = substitutionItems ? RequisitionItemStatus.SUBSTITUTED :
                    it.quantity_revised != null ? RequisitionItemStatus.CHANGED : RequisitionItemStatus.APPROVED

            // Collect to make a deep clone of Available Items, to avoid overriding Available Items from availableItemsMap[it.product_id]
            List<AvailableItem> availableItems = availableItemsMap[it.product_id].collect { AvailableItem availableItem -> new AvailableItem(
                inventoryItem: availableItem.inventoryItem,
                binLocation: availableItem.binLocation,
                quantityAvailable: availableItem.quantityAvailable,
                quantityOnHand: availableItem.quantityOnHand
            )}
            def picklist = (picklistItemsMap && picklistItemsMap[it.product_id]) ? picklistItemsMap[it.product_id] : []
            availableItems = calculateQuantityAvailableToPromise(availableItems, picklist)

            def quantityAvailable = availableItems?.findAll { it.quantityAvailable > 0 }?.sum { it.quantityAvailable }
            def quantityOnHand = availableItems?.sum { it.quantityOnHand }
            def quantityDemandFulfilling = forecastingService.getDemand(requisition.origin, null, productsMap[it.product_id])

            [
                product                     : productsMap[it.product_id],
                productName                 : it.name,
                productCode                 : it.product_code,
                requisitionItemId           : it.id,
                requisition_id              : it.requisition_id,
                quantityRequested           : it.quantity,
                quantityRevised             : it.quantity_revised,
                quantityCanceled            : it.quantity_canceled,
                quantityDemandFulfilling    : quantityDemandFulfilling ? quantityDemandFulfilling.monthlyDemand : 0,
                quantityOnHand              : (quantityOnHand && quantityOnHand > 0 ? quantityOnHand : 0),
                quantityAvailable           : (quantityAvailable && quantityAvailable > 0 ? quantityAvailable : 0),
                quantityCounted             : it.quantity_counted,
                substitutionStatus          : it.substitution_status,
                sortOrder                   : it.sort_order,
                reasonCode                  : it.cancel_reason_code,
                comments                    : it.comments,
                statusCode                  : statusCode.name(),
                substitutionItems           : substitutionItems.collect {
                    Product product = Product.get(it.product_id)
                    List<AvailableItem> availableItemsForSubstitution = productAvailabilityService.getAllAvailableBinLocations(requisition.origin, product?.id)
                    def picklistForSubstitution = (picklistItemsMap && picklistItemsMap[it.product_id]) ? picklistItemsMap[it.product_id] : []
                    availableItemsForSubstitution = calculateQuantityAvailableToPromise(availableItemsForSubstitution, picklistForSubstitution)

                    def qtyAvailable = availableItemsForSubstitution?.findAll { it.quantityAvailable > 0 }?.sum { it.quantityAvailable }
                    def qtyOnHand = availableItemsForSubstitution?.sum { it.quantityOnHand }

                    [
                        product             : product,
                        productId           : it.product_id,
                        productCode         : it.product_code,
                        productName         : it.name,
                        quantityAvailable   : (qtyAvailable && qtyAvailable > 0 ? qtyAvailable : 0),
                        quantityOnHand      : (qtyOnHand && qtyOnHand > 0 ? qtyOnHand : 0),
                        quantitySelected    : it.quantity,
                        quantityRequested   : it.quantity
                    ]
                },
            ]
        }

        return editPageItems
    }

    List<PickPageItem> getPickPageItems(String id, Integer max, Integer offset) {
        List<PickPageItem> pickPageItems = []

        StockMovement stockMovement = getStockMovement(id)

        stockMovement.lineItems.each { stockMovementItem ->
            def items = getPickPageItems(stockMovementItem)
            pickPageItems.addAll(items)
        }

        if (max != null && offset != null) {
            return pickPageItems.subList(offset, offset + max > pickPageItems.size() ? pickPageItems.size() : offset + max);
        }

        return pickPageItems
    }

    List<PackPageItem> getPackPageItems(String id, Integer max, Integer offset) {
        Set<PackPageItem> items = new LinkedHashSet<PackPageItem>()

        StockMovement stockMovement = getStockMovement(id)

        stockMovement.requisition?.picklist?.picklistItems?.sort { a, b ->
            a.sortOrder <=> b.sortOrder ?: a.id <=> b.id
        }?.each { PicklistItem picklistItem ->
            items.addAll(getPackPageItems(picklistItem))
        }

        List<PackPageItem> packPageItems = new ArrayList<PackPageItem>(items)

        if (max != null && offset != null) {
            return packPageItems.subList(offset, offset + max > packPageItems.size() ? packPageItems.size() : offset + max)
        }

        return packPageItems
    }

    List<PicklistItemCommand> parsePickCsvTemplateImport(PicklistImportDataCommand command) {

        try {
            // FIXME We should probably do some additional validation on the import file here or
            //  at least catch any exceptions that might happen
            String text = new String(command.importFile.bytes)

            List<String> fieldKeys = [
                    'id',
                    'code', // code => productCode
                    'name', // name => productName
                    'lotNumber',
                    'expirationDate',
                    'binLocation',
                    'quantity',
            ]
            char separatorChar = CSVUtils.getSeparator(text, fieldKeys.size())

            CSVMapReader csvMapReader = new CSVMapReader(
                    new StringReader(text),
                    [skipLines: 1, separatorChar: separatorChar]
            )
            csvMapReader.fieldKeys = fieldKeys
            return csvMapReader.toList().collect { it ->
                new PicklistItemCommand(
                        id: it.id,
                        code: it.code,
                        name: it.name,
                        lotNumber: it.lotNumber,
                        expirationDate: it.expirationDate ? new Date(it.expirationDate) : null,
                        binLocation: it.binLocation,
                        quantityAsText: it.quantity
                )
            }

        } catch (Exception e) {
            throw new RuntimeException("Error parsing order item CSV: " + e.message, e)
        }
    }

    /**
     * This method needs to be broken down into multiple smaller methods.
     *
     * @param command
     */
    void validatePicklistListImport(PicklistImportDataCommand command) {

        StockMovement stockMovement = command.stockMovement

        // The parent requisition items that picklist items are associated with
        List<PickPageItem> pickPageItems = command.pickPageItems

        // FIXME Remove this if it's no longer being used
        // Configuration property that allows overpick for a given location
        Boolean supportsOverpick = command.location.supports(ActivityCode.ALLOW_OVERPICK)

        // TODO Do we handle the case where ID is null?
        // Group the picklist items from the CSV by requisition item ID
        Map<String, List> picklistItemsGroupedByRequisitionItem = command.picklistItems.groupBy { it.id }

        // Iterate over the parent requisition items
        picklistItemsGroupedByRequisitionItem.each { requisitionItemId, picklistItems ->

            // Iterate over each separate pick
            picklistItems.each { PicklistItemCommand data ->

                // FIXME Make sure we also check that the quantity provided is not greater than the
                //  quantity requested. Should add this to the ImportPickCommand validator.
                Integer quantityRequired = data.quantity

                // FIXME Add proper comment to explain what this is for
                // Base validation, creates errors object?
                data.validate()

                // TODO What happens if ID is null. We should throw an exception if we don't have a good answer.
                //  skip validation if id is empty since most of the validation relies on an existing requisition id
                // skip validation if id is empty since mos tof the validation relies on an existing requisition id
                if (!data.id) {
                    return
                }

                // Find the pick page item by ID (pick page item represents the requisition item)
                PickPageItem pickPageItem = pickPageItems.find { it.requisitionItem?.id == data.id }
                if (!pickPageItem) {
                    data.errors.rejectValue(
                            "id",
                            "importPickCommand.requisitionItem.notFound.error",
                            [data.id] as Object[],
                            "Requisition item id: ${data.id} not found"
                    )
                }
                if (pickPageItem) {

                    // TODO Add guidance for developers - we ALWAYS need to resolve to an inventory
                    //  item when we're working with a lot number.
                    // Resolve given lot number to an inventory item
                    InventoryItem inventoryItem =
                            pickPageItem.requisitionItem.product.getInventoryItem(data.lotNumber, data.expirationDate)

                    // If there's no inventory item for the provide lot number, we throw a validation error
                    if (!inventoryItem) {
                        // FIXME Add error code to messages.properties
                        data.errors.rejectValue(
                                "lotNumber",
                                "importPickCommand.inventoryItem.notFound.error",
                                [data.lotNumber] as Object[],
                                "Unable to find inventory item with lot number {0}")
                    }
                    else {

                        // If there is an inventory item found, let's check to see if there's more
                        // than one picklist item with an empty bin
                        Integer countEmptyBinLocations = picklistItems.findAll { it.lotNumber == inventoryItem.lotNumber }.count { !it.binLocation }
                        if (countEmptyBinLocations > 1) {
                            data.errors.rejectValue(
                                    "binLocation",
                                    "importPickCommand.binLocation.multipleEmpty.error",
                                    [data.lotNumber] as Object[],
                                    "Bin location cannot be empty for the same lot number {0} across multiple rows."
                            )
                        }
                    }

                    // OBPIH-6331 Resolve bin location, if a bin location value was provided. Otherwise,
                    // if the user-provided bin location is null or ambiguous then we need to apply
                    // allocation rules
                    Location internalLocation = command.location.getInternalLocation(data.binLocation)

                    // If a bin location value is provided and it not the default bin location name
                    // FIXME Could add this logic to the command class but I'm not sure what to call
                    //  the method (isUserProvidedButNotDefault is a bit of a mouthful). Open to
                    //  suggestions.
                    if (data.binLocation && !data.binLocation?.equalsIgnoreCase(Constants.DEFAULT_BIN_LOCATION_NAME)) {
                        // ... but the internal location is not found
                        if (!internalLocation) {
                            // FIXME Add error code to messages.properties
                            data.errors.rejectValue(
                                    "binLocation",
                                    "importPickCommand.binLocation.notFound.error",
                                    [data.binLocation] as Object[],
                                    "Unable to find internal location with name {0}")
                        }
                    }

                    // FIXME It's probably ok to use product availability data here. However, we
                    //  probably want to validate against actual inventory items (calculated from
                    //  transactions) before the stock movement is shipped (i.e. transactions created).
                    //  Otherwise we could encounter a situation where the picklist item is valid based on
                    //  the product availability, but this data might be stale for some reason

                    // TODO Need to test whether this satisfies the requirement when passing a NULL
                    //  bin location. We also probably want to deal with the case where the provided
                    //  bin location is provided, but not found.

                    // If there was a bin location provided then we'll want to check if there's an
                    // available item at that location
                    if (data.binLocation) {

                        // Let's try to determine whether there's a specific available inventory item
                        //  associated with the lot number and bin data provided by the user.
                        AvailableItem availableItem = pickPageItem.getAvailableItem(inventoryItem, internalLocation)
                        ApplicationTagLib g = grailsApplication.mainContext.getBean(ApplicationTagLib.class)

                        // FIXME This is only necessary if we cannot find a happy case from OBPIH-6331
                        if (!availableItem) {
                            String lotNumberName = data.lotNumber ?: g.message(code: "default.noLotNumber.label", default: Constants.DEFAULT_LOT_NUMBER)
                            String binLocationName = data.binLocation ?: g.message(code: "default.noBinLocation.label", default: Constants.DEFAULT_BIN_LOCATION_NAME)
                            data.errors.rejectValue(
                                    "id",
                                    "importPickCommand.availableItem.notFound.error",
                                    [lotNumberName, binLocationName] as Object[],
                                    "There is no available item for lot number {0} and bin location {1}"
                            )
                        }

                        // If there is an available item for the location but it's unavailable
                        if (availableItem && !availableItem.isQuantityPickable(data?.quantity)) {
                            String lotNumberName = data.lotNumber ?: g.message(code: "default.noLotNumber.label", default: Constants.DEFAULT_LOT_NUMBER)
                            String binLocationName = data.binLocation ?: g.message(code: "default.noBinLocation.label", default: Constants.DEFAULT_BIN_LOCATION_NAME)
                            data.errors.rejectValue(
                                    "id",
                                    "importPickCommand.quantity.insuffientQuantityAvailable",
                                    [lotNumberName, binLocationName, availableItem.quantityAvailable, data.quantity] as Object[],
                                    "Insufficient quantity available for lot number {0} " +
                                            "and bin location {1} [Available: {2}, Required: {3}]. Please review pick."
                            )
                        }
                    }

                    // OBPIH-6331 This is the main driver for the infer bin location mechanism
                    // If the internal location is NULL at this point, that means the user left
                    // the cell blank and would like the system to allocate based on our rules
                    if (!data.binLocation && !internalLocation) {

                        // FIXME THis feels a bit weird because we should allow the allocation
                        //  algorithm to sort and decide which location is best
                        // First attempt to get the available items in the default location
                        //List<AvailableItem> availableItems = pickPageItem.getAvailableItemsInDefaultLocation(inventoryItem)

                        // It was in fact premature to be doing the above filtering as it was
                        // causing some major issues when there were available items in multiple locations
                        List<AvailableItem> availableItems =
                                pickPageItem.getAvailableItems(inventoryItem)

                        // Create an allocation request and validate that it
                        AllocationRequest allocationRequest = new AllocationRequest(
                                product: inventoryItem?.product,
                                inventoryItem: inventoryItem,
                                picklistItemCommand: data,
                                availableItems: availableItems,
                                quantityRequired: quantityRequired)

                        // Validate allocation request
                        validateAllocationRequest(allocationRequest)

                        // Add validation errors to command object
                        if (allocationRequest.hasErrors()) {
                            allocationRequest.errors.allErrors.each { it
                                data.errors.reject(it.code)
                            }
                        }
                    }

                    // FIXME This validation should happen only on the first line for the
                    //  requisition item. I tried to implement it in the outer loop but had trouble
                    //  with adding errors to command object. The side-effect of not fixing this
                    //  is that we have to see this error for every row which is infinitely better
                    //  than not seeing it.
                    Integer totalQuantityPicked = picklistItemsGroupedByRequisitionItem[data.id].sum { it.quantity }

                    // TODO Should we really be using requisitonItem.quantity here?
                    if (totalQuantityPicked > pickPageItem.requisitionItem.quantity) {
                        Boolean allowsOverPick = stockMovement.origin.supports(ActivityCode.ALLOW_OVERPICK)
                        if (!allowsOverPick) {
                            data.errors.rejectValue(
                                    "quantity",
                                    "importPickCommand.quantity.error",
                                    [totalQuantityPicked, data.code, quantityRequired] as Object[],
                                    "Total quantity picked {0} for product {1} must match the expected quantity {2}. Please review pick."
                            )
                        }
                    } else if (totalQuantityPicked != pickPageItem.requisitionItem.quantity) {
                        data.errors.rejectValue(
                                "quantity",
                                "importPickCommand.quantity.error",
                                [totalQuantityPicked, data.code, quantityRequired] as Object[],
                                "Total quantity picked {0} for product {1} must match the expected quantity {2}. Please review pick."
                        )
                    }
                }
            }
        }
    }

    // TODO Test whether the sum of an empty available items list returns 0
    //  If we want to be more strict we can check to see if there's enough quantity in at least
    //  one item
    boolean validateQuantityAvailable(List<AvailableItem> availableItems, Integer quantityRequired) {
        return availableItems?.any { it.quantityAvailable >= quantityRequired }
    }

    void validateAllocationRequest(AllocationRequest allocationRequest) {

        Product product = allocationRequest.product
        InventoryItem inventoryItem = allocationRequest.inventoryItem
        Integer quantityRequired = allocationRequest.quantityRequired
        List<AvailableItem> availableItems = allocationRequest.availableItems
        PicklistItemCommand picklistItemCommand = allocationRequest.picklistItemCommand

        // Building blocks for validation logic
        // FIXME Could probably move these to custom validators on AllocationRequest
        Integer countInBinLocations = availableItems.findAll { it.isPhysicalLocation }?.size()
        boolean isInDefaultLocation = availableItems.every { it.isDefaultLocation }
        boolean isInReceivingLocations = availableItems.every { it.isReceivingLocation }
        boolean isInBinLocations = availableItems.every { it.isPhysicalLocation && !it.isDefaultLocation }
        boolean hasAllHoldLocations = !availableItems.empty ? availableItems.every { it.onHold } : false
        boolean hasAnyPhysicalLocations = availableItems?.any { it?.isPhysicalLocation }
        boolean hasAnyVirtualLocations = availableItems?.any { it?.isVirtualLocation }

        // Scenario 4: Any other scenario (stock in “real” bin and virtual bin, stock in multiple real bins)
        if (hasAnyPhysicalLocations && hasAnyVirtualLocations) {
            picklistItemCommand.errors.rejectValue("binLocation",
                    "importPickCommand.availableItems.inMultipleBinLocations",
                    [product.productCode, inventoryItem?.lotNumber, availableItems.binLocationName] as Object [],
                    "Product {0} with lot number {1} has stock in multiple physical and virtual locations {2}. Please indicate the bin location to pick from."
            )
        }

        // Scenario 2: All stock in one bin (or in one real bin and 1 or more hold bins - ignore hold bins)
        else if (countInBinLocations > 1) {
            picklistItemCommand.errors.rejectValue("binLocation",
                    "importPickCommand.availableItems.inMultipleLocations",
                    [product.productCode, inventoryItem?.lotNumber, availableItems.binLocationName] as Object [],
                    "Product {0} with lot number {1} has stock in multiple bin locations {2}. Please indicate the bin location to pick from."
            )
        }

        // FIXME I'm not sure if it's necessary, but I couldn't figure out a way to handle
        //  validation where stock is not in the default or receiving locations (needs to take
        //  bin locations into account, but couldn't get the logic right.
        // Scenario 1: All stock in default (no bin)
//        if (!isAllStockInReceivingOrDefault) {
//            picklistItemCommand.errors.rejectValue("binLocation",
//                    "allocationRequest.availableItems.notInReceivingOrDefaultLocation",
//                    [product.productCode, availableItems.binLocationName] as Object [],
//                    "Product {0} must only have stock in a single bin location, the default location or receiving locations {1}"
//            )
//        }

        // Scenario 3: All stock in receiving or default bin > if lot has stock entries only in hold bin > invalid
        else if (hasAllHoldLocations) {
            picklistItemCommand.errors.rejectValue("binLocation",
                    "allocationRequest.availableItems.inHoldLocations",
                    [product.productCode, inventoryItem?.lotNumber, availableItems.binLocationName] as Object [],
                    "Product {0} with lot number {1} only has stock in hold locations."
            )
        }

        // FIXME This might need to be moved to the code that actually filters the available item
        //  otherwise it probably doesn't make sense in this context. It's probably not hurting
        //  anything, but it's definitely not doing what I originally intended for it to do.

        // Validate the quantity available for all available items
        boolean canAllocateQuantity = validateQuantityAvailable(availableItems, quantityRequired)
        if (!canAllocateQuantity) {
            Integer quantityAvailable = availableItems.quantityAvailable.max()?:0
            picklistItemCommand.errors.rejectValue(
                "quantity",
                "importPickCommand.quantity.insuffientQuantityAvailable",
                [picklistItemCommand.code, picklistItemCommand.lotNumber?:Constants.DEFAULT_LOT_NUMBER, quantityAvailable, quantityRequired] as Object[],
                "Insufficient quantity available for product code {0} and lot number {1} " +
                    "[Maximum Available: {2}, Required: {3}]. Please review pick.")
        }

    }

    // FIXME not sure if we should return available items or suggested items
    // TODO SO the idea here is that we want to first apply filtering rules to the available items
    //  according to the ticket (OBPIH-6331) and then have another method actually check whether
    //  there's enough quantity in these locations. I was initially trying to do everything at once
    //  but it got really confusing because we would have to apply a rule and then check, apply
    //  the next rule and check, on and on. Eventually I want these to be implemented using the
    //  Strategy pattern but I was trying hard not to implement that here.
    // FIXME One other issue here is that it would be much easier to implement if we had a parent
    //  parent object above available items that was responsible for answering questions
    List<AvailableItem> applyAllocationRulesOnAvailableItems(List<AvailableItem> availableItems, Integer quantityRequired) {

        // TODO Remove unnecessary logging before merging
        log.info "Apply allocation rules on available items "
        log.info "Quantity Required = " + quantityRequired
        log.info "Available items = " + availableItems.size()

        availableItems.each {
            log.info " - Available item " + it.toJson()
        }

        // filter out hold locations
        availableItems = availableItems.findAll { !it.isOnHold() }

        // If there's only one pickable location, it should either be a default location or
        if (availableItems.count { it.pickable } == 1) {

            // Scenario 1: All stock in default (no bin)
            // TODO Could we have more than one available items in a default location?
            // TODO If there's enough available quantity in the default bin, then allocate
            //  quantity from the default location
            boolean isInDefaultLocationOrReceivingLocation = availableItems.every { it.isDefaultLocation || it.isReceivingLocation }
            if (isInDefaultLocationOrReceivingLocation) {
                return availableItems
            }

            // Scenario 2: All stock in one bin
            // TODO If there's enough available quantity in one internal location, then
            //  then allocate stock from that location. Otherwise return an error?
            boolean isInPhysicalLocation = availableItems.every { it.isPhysicalLocation }
            if (isInPhysicalLocation) {
                return availableItems
            }
        }
        // If there are more than one internal locations, then we apply the following rules
        else {

            // Scenario 3a: if lot has stock entries in receiving bins
            // TODO Are receiving locations configured to be picking locations?
            // TODO If there's available quantity in default + receiving locations, then
            //  allocate using FIFO algorithm. Need to check with Manon if this is what is
            //  expected.
            boolean isAllStockInReceivingLocations = availableItems.every { it.isReceivingLocation }
            if (isAllStockInReceivingLocations) {
                return availableItems
            }
            // Scenario 3b: if lot has stock entries in default bin
            // TODO If there's enough quantity available in default location, then
            //  allocate from default location.

            // Scenario 3c: if lot has stock entries only in hold bins
            // TODO If lot has stock entries only in hold bin, then throw an exception.

            // Scenario 4: Any other scenario (stock in “real” bin and virtual bin, stock in multiple real bins)
            // TODO If there's not enough available quantity in default + receiving
            //  locations, then throw an exception
            boolean isAllStockInReceivingOrDefault = availableItems.every { it.isReceivingLocation || it.isDefaultLocation }
            if (isAllStockInReceivingOrDefault) {
                return availableItems
            }
        }

        // FIXME Consider throwing an exception here because we've fallen through all
        //  of the expected scenarios
        return []
    }

    void importPicklistItems(PicklistImportDataCommand command) {

        StockMovement stockMovement = command.stockMovement
        List<PickPageItem> pickPageItems = command.pickPageItems
        List<PicklistItemCommand> picklistItems = command.picklistItems

        Map<String, List<PicklistItemCommand>> picklistItemsGroupedByRequisitionItem = picklistItems.groupBy { it.id }

        picklistItemsGroupedByRequisitionItem.each { String requisitionItemId, List<PicklistItemCommand> picklistItemsToImport  ->
            // skip rows with errors
            if (picklistItemsToImport.any{ it.hasErrors() }) {
                return
            }

            PickPageItem pickPageItem = pickPageItems.find {
                it.requisitionItem?.id == requisitionItemId
            }

            // TODO Decide whether we want to use revert pick or clear picklist (below)
            //if (pickPageItem?.requisitionItem?.picklistItems?.size()) {
            //    picklistService.revertPick(requisitionItemId)
            //}

            // TODO Does the order of operations matter (remove shipment items vs picklist items)
            // Remove existing shipment items and picklist items
            removeShipmentItemsForModifiedRequisitionItem(pickPageItem.requisitionItem)

            // TODO Need to test this thoroughly as it does not feel like a good idea. For example,
            //  we don't necessarily want to clear picklist if there's no picklist items to import
            //  so this might need to go inside a check on !picklistItemsToImport.empty
            clearPicklist(pickPageItem.requisitionItem)

            // Iterate over all of the picklist items to import and attempt to create a picklist item
            picklistItemsToImport.each { params ->

                InventoryItem inventoryItem = pickPageItem.requisitionItem.product.getInventoryItem(params.lotNumber, params.expirationDate)
                Location internalLocation = command.location.getInternalLocation(params.binLocation)

                // Get the available item for the provide lot number and bin location
                AvailableItem availableItem = pickPageItem.getAvailableItem(inventoryItem, internalLocation)

                // If there is an available item with enough we allocate that item to the outbound order
                if (availableItem) {
                    Picklist picklist = stockMovement.requisition?.picklist
                    // find existing picklist to update
                    PicklistItem picklistItem = picklist.picklistItems.find {
                        it.inventoryItem?.id == availableItem.inventoryItem?.id &&
                                it.binLocation?.id == availableItem.binLocation?.id
                    }
                    createOrUpdatePicklistItem(
                            pickPageItem.requisitionItem,
                            picklistItem,
                            availableItem.inventoryItem,
                            availableItem.binLocation,
                            params.quantity,
                            null,
                            null,
                            false,
                    )

                }
                // Otherwise we may need to allocate a new item based on the rules
                else {
                    // If there's no internal location then we should apply allocation rules to find
                    // any available items that might satisfy the quantity required
                    if (!internalLocation) {

                        // TODO This allocation deserves its own method somewhere, probably on
                        //  a separate service.
                        List<AvailableItem> availableItems = pickPageItem.getAvailableItems(inventoryItem)
                        availableItems = applyAllocationRulesOnAvailableItems(availableItems, params.quantity)
                        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, params.quantity)
                        allocateSuggestedItems(pickPageItem.requisitionItem, suggestedItems, false)
                    }
                }
            }
            createMissingShipmentItem(pickPageItem.requisitionItem)
        }
    }

    List<ReceiptItem> getStockMovementReceiptItems(def stockMovement) {
        return (stockMovement.requisition) ?
                getRequisitionBasedStockMovementReceiptItems(stockMovement) :
                getShipmentBasedStockMovementReceiptItems(stockMovement)
    }

    List<ReceiptItem> getRequisitionBasedStockMovementReceiptItems(def stockMovement) {
        def shipments = Shipment.findAllByRequisition(stockMovement.requisition)
        List<ReceiptItem> receiptItems = shipments*.receipts?.flatten()*.sortReceiptItemsBySortOrder()?.flatten()
        return receiptItems
    }

    List<ReceiptItem> getShipmentBasedStockMovementReceiptItems(def stockMovement) {
        Shipment shipment = stockMovement.shipment
        List<ReceiptItem> receiptItems = shipment.receipts*.sortReceiptItemsBySortOrder()?.flatten()
        return receiptItems
    }

    // It expects to receive a stock movement id
    void clearPicklist(String id) {
        StockMovement stockMovement = getStockMovement(id)
        clearPicklist(stockMovement)
    }

    void clearPicklist(StockMovement stockMovement) {
        for (StockMovementItem stockMovementItem : stockMovement.lineItems) {
            clearPicklist(stockMovementItem)
        }
    }

    void clearPicklist(StockMovementItem stockMovementItem) {
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        clearPicklist(requisitionItem)
    }

    void clearPicklist(RequisitionItem requisitionItem) {
        if (requisitionItem.modificationItem) {
            requisitionItem = requisitionItem.modificationItem
        }

        if (requisitionItem.pickReasonCode) {
            requisitionItem.pickReasonCode = null
        }

        Picklist picklist = requisitionItem?.requisition?.picklist
        if (picklist) {
            log.info "Clear picklist"
            def binLocations = []
            picklist.picklistItems.findAll {
                it.requisitionItem == requisitionItem
            }.toArray().each {
                it.disableRefresh = Boolean.TRUE
                picklist.removeFromPicklistItems(it)
                binLocations << it.binLocation?.id
                requisitionItem.removeFromPicklistItems(it)
                it.delete()
            }
            picklist.save()

            // Save requisition item before PA refresh
            requisitionItem.save(flush: true)

            productAvailabilityService.refreshProductsAvailability(requisitionItem?.requisition?.origin?.id, [requisitionItem?.product?.id], binLocations?.unique(), false)
        } else {
            requisitionItem.save(flush: true)
        }
    }

    void createMissingPicklistItems(StockMovement stockMovement) {
        if (!stockMovement?.origin?.isSupplier() && stockMovement?.origin?.supports(ActivityCode.MANAGE_INVENTORY) && stockMovement.requisition?.status >= RequisitionStatus.PICKING) {
            stockMovement?.lineItems?.each { StockMovementItem stockMovementItem ->
                if (stockMovementItem.statusCode == 'SUBSTITUTED') {
                    for (StockMovementItem subStockMovementItem : stockMovementItem.substitutionItems) {
                        createMissingPicklistItems(subStockMovementItem)
                    }
                } else if (stockMovementItem.statusCode == 'CHANGED') {
                    if (!stockMovementItem.requisitionItem?.modificationItem?.picklistItems) {
                        createMissingPicklistItems(stockMovementItem)
                    }
                } else {
                    createMissingPicklistItems(stockMovementItem)
                }
            }
        }
    }

    void createMissingPicklistForStockMovementItem(StockMovementItem stockMovementItem) {
        if (stockMovementItem?.requisitionItem?.requisition?.status < RequisitionStatus.PICKING) {
            return
        }

        if (stockMovementItem.statusCode == 'SUBSTITUTED') {
            for (StockMovementItem subStockMovementItem : stockMovementItem.substitutionItems) {
                createMissingPicklistItems(subStockMovementItem)
            }
        } else if (stockMovementItem.statusCode == 'CHANGED') {
            if (!stockMovementItem.requisitionItem?.modificationItem?.picklistItems) {
                createMissingPicklistItems(stockMovementItem)
            }
        } else {
            createMissingPicklistItems(stockMovementItem)
        }
    }

    void createMissingPicklistItems(StockMovementItem stockMovementItem) {
        if (!stockMovementItem.requisitionItem?.picklistItems) {
            createPicklist(stockMovementItem, false)
        }
    }

    /**
     * Create an automated picklist for the stock movenent associated with the given id.
     *
     * @param id
     */
    void createPicklist(String id) {
        StockMovement stockMovement = getStockMovement(id)
        for (RequisitionItem requisitionItem : stockMovement.requisition.requisitionItems) {
            removeShipmentItemsForModifiedRequisitionItem(requisitionItem)
        }
        createPicklist(stockMovement)
        for (RequisitionItem requisitionItem : stockMovement.requisition.requisitionItems) {
            createMissingShipmentItem(requisitionItem)
        }
    }

    // TODO: Refactor - Move entire picklist logic to the picklistService
    /**
     * Create an automated picklist for the given stock movement.
     *
     * @param stockMovement
     */
    void createPicklist(StockMovement stockMovement) {
        for (StockMovementItem stockMovementItem : stockMovement.lineItems) {
            if (stockMovementItem.statusCode == 'SUBSTITUTED') {
                for (StockMovementItem subStockMovementItem : stockMovementItem.substitutionItems) {
                    createPicklist(subStockMovementItem, true)
                }
            } else {
                createPicklist(stockMovementItem, true)
            }
        }
    }

    void createPicklist(StockMovementItem stockMovementItem, Boolean validateQtyAvailable) {
        log.info "Create picklist for stock movement item ${stockMovementItem.toJson()}"

        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        createPicklist(requisitionItem, validateQtyAvailable)
    }

    /**
     * Create an automated picklist for the given stock movement item.
     *
     * @param id
     */
    void createPicklist(RequisitionItem requisitionItem, validateQtyAvailable) {
        Location location = requisitionItem?.requisition?.origin
        Integer quantityRequired = requisitionItem?.calculateQuantityRequired()

        log.info "QUANTITY REQUIRED: ${quantityRequired}"

        if (quantityRequired) {
            // Retrieve all available items and then calculate suggested
            List<AvailableItem> availableItems = getAvailableItems(location, requisitionItem)
            log.info "Available items: ${availableItems}"
            List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequired)
            log.info "Suggested items " + suggestedItems
            clearPicklist(requisitionItem)
            if (suggestedItems) {
                for (SuggestedItem suggestedItem : suggestedItems) {
                    createOrUpdatePicklistItem(requisitionItem,
                            null,
                            suggestedItem.inventoryItem,
                            suggestedItem.binLocation,
                            suggestedItem.quantityPicked.intValueExact(),
                            null,
                            null,
                            true
                    )
                }
            }
            if (validateQtyAvailable && !suggestedItems) {
                String errorMessage = "Product " + requisitionItem.product.productCode + " has no available inventory. Please go back to edit page and revise quantity"
                requisitionItem.errors.rejectValue("picklistItems", errorMessage, [
                        requisitionItem?.product?.productCode,
                ].toArray(), errorMessage)

                // FIXME: consider not allowing zeroing out picked stock
                throw new ValidationException(errorMessage, requisitionItem.errors)
            }
        }
    }

    void createOrUpdatePicklistItem(StockMovementItem stockMovementItem, PicklistItem picklistItem,
                                    InventoryItem inventoryItem, Location binLocation,
                                    Integer quantity, String reasonCode, String comment) {

        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        createOrUpdatePicklistItem(requisitionItem, picklistItem, inventoryItem, binLocation, quantity, reasonCode, comment)
    }

    void createOrUpdatePicklistItem(RequisitionItem requisitionItem, PicklistItem picklistItem,
                                    InventoryItem inventoryItem, Location binLocation,
                                    Integer quantity, String reasonCode, String comment, Boolean isAutoAllocated = true) {

        Requisition requisition = requisitionItem.requisition

        Picklist picklist = Picklist.findByRequisition(requisition)
        if (!picklist) {
            picklist = new Picklist()
            picklist.requisition = requisition
            requisition.picklist = picklist
        }

        // If one does not exist create it and add it to the list
        if (!picklistItem) {
            picklistItem = new PicklistItem()
            picklist.addToPicklistItems(picklistItem)
        }

        // Set pick reason code if it is different than the one that has already been added to the item
        if (reasonCode && requisitionItem.pickReasonCode != reasonCode) {
            requisitionItem.pickReasonCode = reasonCode
        }

        requisitionItem.autoAllocated = isAutoAllocated

        // Remove from picklist
        if (quantity == null) {
            picklist.removeFromPicklistItems(picklistItem)
        }
        // Populate picklist item
        else {

            // If we've modified the requisition item we need to associate picks with the modified item
            if (requisitionItem.modificationItem) {
                requisitionItem = requisitionItem.modificationItem
            }
            requisitionItem.addToPicklistItems(picklistItem)
            picklistItem.inventoryItem = inventoryItem
            picklistItem.binLocation = binLocation
            picklistItem.quantity = quantity
            picklistItem.reasonCode = reasonCode
            picklistItem.comment = comment
            picklistItem.sortOrder = requisitionItem.orderIndex
            picklistItem.disableRefresh = Boolean.TRUE
            picklistItem.sortOrder = requisitionItem.orderIndex
        }
        picklist.save(flush: true)

        productAvailabilityService.refreshProductsAvailability(requisitionItem?.requisition?.origin?.id, [inventoryItem?.product?.id], [binLocation?.id], false)
    }

    void createOrUpdatePicklistItem(StockMovement stockMovement, List<PickPageItem> pickPageItems) {

        Requisition requisition = stockMovement.requisition
        Picklist picklist = requisition?.picklist
        if (!picklist) {
            picklist = new Picklist()
            picklist.requisition = requisition
            requisition.picklist = picklist
        }
        pickPageItems.each { pickPageItem ->
            pickPageItem.picklistItems?.toArray()?.each { PicklistItem picklistItem ->
                // If one does not exist add it to the list
                if (!picklistItem.id) {
                    picklist.addToPicklistItems(picklistItem)
                }

                // Remove from picklist
                if (picklistItem.quantity <= 0) {
                    picklist.removeFromPicklistItems(picklistItem)
                    picklistItem.requisitionItem?.removeFromPicklistItems(picklistItem)
                }
            }
        }

        // FIXME Check to see if both of these are needed
        requisition.save()
        picklist.save()
    }

    Set<PicklistItem> getPicklistItems(RequisitionItem requisitionItem) {
        if (requisitionItem.modificationItem) {
            requisitionItem = requisitionItem.modificationItem
        }

        Picklist picklist = requisitionItem?.requisition?.picklist

        if (picklist) {
            return picklist.picklistItems.findAll {
                it.requisitionItem == requisitionItem
            }
        }

        return []
    }

    void updatePicklistItem(StockMovementItem stockMovementItem, List picklistItems, String reasonCode) {
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        Boolean isAutoAllocated = requisitionItem.autoAllocated ?: false

        clearPicklist(requisitionItem)

        picklistItems.each { picklistItemMap ->

            PicklistItem picklistItem = picklistItemMap.id ?
                    PicklistItem.get(picklistItemMap.id) : null

            InventoryItem inventoryItem = picklistItemMap.inventoryItem?.id ?
                    InventoryItem.get(picklistItemMap.inventoryItem?.id) : null

            Location binLocation = picklistItemMap.binLocation?.id ?
                    Location.get(picklistItemMap.binLocation?.id) : null

            BigDecimal quantityPicked = (picklistItemMap.quantityPicked != null && picklistItemMap.quantityPicked != "") ?
                    new BigDecimal(picklistItemMap.quantityPicked) : null

            String comment = picklistItemMap.comment

            createOrUpdatePicklistItem(requisitionItem, picklistItem, inventoryItem, binLocation,
                    quantityPicked?.intValueExact(), reasonCode, comment, isAutoAllocated)
        }
    }

    List<AvailableItem> getAvailableItems(Location location, RequisitionItem requisitionItem) {
        return getAvailableItems(location, requisitionItem, false)
    }

    List<AvailableItem> getAvailableItems(Location location, RequisitionItem requisitionItem, Boolean calculateStatus) {
        List<AvailableItem> availableItems = productAvailabilityService.getAllAvailableBinLocations(location, requisitionItem.product?.id)
        def picklistItems = getPicklistItems(requisitionItem)

        availableItems = availableItems.findAll { it.quantityOnHand > 0 }
        availableItems = calculateQuantityAvailableToPromise(availableItems, picklistItems)

        if (calculateStatus) {
            return calculateAvailableItemsStatus(requisitionItem, availableItems)
        }

        return productAvailabilityService.sortAvailableItems(availableItems)
    }

    List<AvailableItem> calculateQuantityAvailableToPromise(List<AvailableItem> availableItems, def picklistItems) {
        for (PicklistItem picklistItem : picklistItems) {
            AvailableItem availableItem = availableItems.find {
                it.inventoryItem == picklistItem.inventoryItem && it.binLocation == picklistItem.binLocation
            }

            if (!availableItem) {
                availableItem = new AvailableItem(
                        inventoryItem: picklistItem.inventoryItem,
                        binLocation: picklistItem.binLocation,
                        quantityAvailable: 0,
                        quantityOnHand: 0
                )

                availableItems.add(availableItem)
            } else {
                availableItem.quantityAvailable += picklistItem.quantity
            }
        }

        return availableItems
    }

    List<AvailableItem> calculateAvailableItemsStatus(RequisitionItem requisitionItem, List<AvailableItem> availableItems) {
        return availableItems?.collect {
            if (it.status == AvailableItemStatus.AVAILABLE || it.status == AvailableItemStatus.PICKED) {
                def picklists = getPicklistByLocationAndProduct(it.binLocation, it.inventoryItem)
                List<String> pickedRequisitionNumbers = picklists?.collect { it.requisition.requestNumber }?.unique()?.findAll {
                    it != requisitionItem.requisition.requestNumber }

                it.pickedRequisitionNumbers = pickedRequisitionNumbers
            }

            return it
        }
    }

    def getPicklistByLocationAndProduct(Location binLocation, InventoryItem inventoryItem) {
        return Picklist.createCriteria().list {
            requisition {
                'in'("status", RequisitionStatus.listPending())
            }
            picklistItems {
                eq("inventoryItem", inventoryItem)
                eq("binLocation", binLocation)
            }
        }
    }

    /**
     * Get a list of suggested items for the given stock movement item.
     *
     * @param stockMovementItem
     * @return
     */
    List getSuggestedItems(List<AvailableItem> availableItems, Integer quantityRequested) {

        List suggestedItems = []
        List<AvailableItem> autoPickableItems = availableItems?.findAll { it.quantityAvailable > 0 && it.pickable }

        // As long as quantity requested is less than the total available we can iterate through available items
        // and pick until quantity requested is 0. Otherwise, we don't suggest anything because the user must
        // choose anyway. This might be improved in the future.
        Integer quantityAvailable = autoPickableItems ? autoPickableItems?.sum {
            it.quantityAvailable
        } : 0
        if (quantityAvailable > 0) {

            for (AvailableItem availableItem : autoPickableItems) {
                if (quantityRequested == 0)
                    break

                // The quantity to pick is either the quantity available (if less than requested) or
                // the quantity requested (if less than available).
                int quantityPicked = (quantityRequested >= availableItem.quantityAvailable) ?
                        availableItem.quantityAvailable : quantityRequested

                log.info "Suggested quantity ${quantityPicked}"
                suggestedItems << new SuggestedItem(inventoryItem: availableItem?.inventoryItem,
                        binLocation: availableItem?.binLocation,
                        quantityAvailable: availableItem?.quantityAvailable,
                        quantityRequested: quantityRequested,
                        quantityPicked: quantityPicked)
                quantityRequested -= quantityPicked
            }
        }
        return suggestedItems
    }

    List<SubstitutionItem> getAvailableSubstitutions(Location location, RequisitionItem requisitionItem) {
        Product product = requisitionItem.product
        List<SubstitutionItem> availableSubstitutions

        if (location) {
            def productAssociations =
                    productService.getProductAssociations(product, [ProductAssociationTypeCode.SUBSTITUTE])

            availableSubstitutions = productAssociations.collect { productAssociation ->

                def associatedProduct = productAssociation.associatedProduct
                def availableItems

                if (requisitionItem.substitutionItems) {
                    def picklistItems = requisitionItem.substitutionItems.findAll { it.product == associatedProduct }
                            .collect { it.picklistItems }?.flatten()

                    availableItems = productAvailabilityService.getAvailableBinLocations(location, associatedProduct?.id)
                    availableItems = calculateQuantityAvailableToPromise(availableItems, picklistItems)
                    availableItems = productAvailabilityService.sortAvailableItems(availableItems)
                } else {
                    availableItems = productAvailabilityService.getAvailableBinLocations(location, associatedProduct?.id)
                }

                log.info "Available items for substitution ${associatedProduct}: ${availableItems}"
                SubstitutionItem substitutionItem = new SubstitutionItem()
                substitutionItem.product = associatedProduct
                substitutionItem.productId = associatedProduct.id
                substitutionItem.productName = associatedProduct.name
                substitutionItem.productCode = associatedProduct.productCode
                substitutionItem.availableItems = availableItems
                return substitutionItem
            }
        }

        return availableSubstitutions.findAll { availableItems -> availableItems.quantityAvailable > 0 }
    }


    void allocateSuggestedItems(RequisitionItem requisitionItem, List<SuggestedItem> suggestedItems, Boolean isAutoAllocated = true) {

        for (SuggestedItem suggestedItem : suggestedItems) {
            createOrUpdatePicklistItem(
                    requisitionItem,
                    null,
                    suggestedItem.inventoryItem,
                    suggestedItem.binLocation,
                    suggestedItem.quantityPicked?.intValueExact(),
                    null,
                    null,
                    isAutoAllocated
            )
        }
    }

    void allocatePicklistItems(List<RequisitionItem> requisitionItems) {
        requisitionItems.each { RequisitionItem requisitionItem ->
            if (requisitionItem.isSubstituted()) {
                requisitionItem.substitutionItems.collect { allocateMissingPicklistItems(it) }
            } else if (requisitionItem.modificationItem) {
                allocateMissingPicklistItems(requisitionItem.modificationItem)
            } else {
                if (!requisitionItem.isCanceled()) {
                    allocateMissingPicklistItems(requisitionItem)
                }
            }
        }
    }

    /**
     * Get a list of pick page items for the given stock movement item.
     *
     * @param stockMovementItem
     * @return
     */
    List getPickPageItems(StockMovementItem stockMovementItem) {
        List pickPageItems = []
        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)
        if (requisitionItem.isSubstituted()) {
            pickPageItems = requisitionItem.substitutionItems.sort { it.orderIndex }. collect {
                return buildPickPageItem(it, stockMovementItem.sortOrder)
            }
        } else if (requisitionItem.modificationItem) {
            pickPageItems << buildPickPageItem(requisitionItem.modificationItem, stockMovementItem.sortOrder)
        } else {
            if (!requisitionItem.isCanceled()) {
                pickPageItems << buildPickPageItem(requisitionItem, stockMovementItem.sortOrder)
            }
        }
        return pickPageItems
    }

    PickPageItem buildPickPageItem(RequisitionItem requisitionItem, Integer sortOrder) {
        return buildPickPageItem(requisitionItem, sortOrder, false)
    }

    /**
     *
     * @param requisitionItem
     * @return
     */
    PickPageItem buildPickPageItem(RequisitionItem requisitionItem, Integer sortOrder, Boolean showDetails) {
        PickPageItem pickPageItem = new PickPageItem(requisitionItem: requisitionItem,
                picklistItems: requisitionItem.picklistItems)
        Location location = requisitionItem?.requisition?.origin

        List<AvailableItem> availableItems = getAvailableItems(location, requisitionItem, showDetails)
        Integer quantityRequired = requisitionItem?.calculateQuantityRequired()
        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequired)
        pickPageItem.availableItems = availableItems
        pickPageItem.suggestedItems = suggestedItems
        pickPageItem.sortOrder = requisitionItem.orderIndex ?: sortOrder

        return pickPageItem
    }

    void allocateMissingPicklistItems(RequisitionItem requisitionItem) {
        Boolean hasPicklistItems = requisitionItem.picklistItems
        Boolean isQuantityPickedOtherThanRequested = requisitionItem.totalQuantityPicked() != requisitionItem.quantity
        Boolean hasReasonCode = requisitionItem.picklistItems.reasonCode

        if (!hasPicklistItems || (hasPicklistItems && isQuantityPickedOtherThanRequested && !hasReasonCode)) {
            createPicklist(requisitionItem, false)
        }
    }

    List getPackPageItems(PicklistItem picklistItem) {
        List packPageItems = []
        List<ShipmentItem> shipmentItems = ShipmentItem.findAllByRequisitionItem(picklistItem?.requisitionItem)
        if (shipmentItems) {
            shipmentItems.sort { a, b ->
                a.sortOrder <=> b.sortOrder ?: b.id <=> a.id
            }.each { shipmentItem ->
                packPageItems << buildPackPageItem(shipmentItem)
            }
        }

        return packPageItems
    }

    PackPageItem buildPackPageItem(ShipmentItem shipmentItem) {
        String palletName = ""
        String boxName = ""
        if (shipmentItem?.container?.parentContainer) {
            palletName = shipmentItem?.container?.parentContainer?.name
            boxName = shipmentItem?.container?.name
        } else if (shipmentItem?.container) {
            palletName = shipmentItem?.container?.name
        }

        return new PackPageItem(shipmentItem: shipmentItem, palletName: palletName, boxName: boxName)
    }

    StockMovement createShipmentBasedStockMovement(StockMovement stockMovement) {
        Shipment shipment = createInboundShipment(stockMovement)
        return StockMovement.createFromShipment(shipment)
    }

    Shipment createInboundShipment(ShipOrderCommand command) {
        Order order = command.order
        Shipment shipment = new Shipment()
        shipment.expectedShippingDate = new Date()
        shipment.name = order.name ?: order.orderNumber
        shipment.description = order.orderNumber
        shipment.origin = order.origin
        shipment.destination = order.destination
        shipment.createdBy = order.orderedBy
        shipment.shipmentType = ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID)
        shipment.shipmentNumber = shipmentIdentifierService.generate(shipment)

        command.shipOrderItems.each { ShipOrderItemCommand orderItemCommand ->
            if (orderItemCommand.quantityToShip > 0) {
                OrderItem orderItem = orderItemCommand.orderItem
                ShipmentItem shipmentItem = new ShipmentItem()
                shipmentItem.lotNumber = orderItemCommand?.inventoryItem?.lotNumber
                shipmentItem.expirationDate = orderItemCommand?.inventoryItem?.expirationDate
                shipmentItem.product = orderItemCommand.orderItem.product
                shipmentItem.inventoryItem = orderItemCommand.inventoryItem
                shipmentItem.quantity = orderItemCommand.quantityToShip * orderItemCommand.orderItem.quantityPerUom
                shipmentItem.recipient = orderItemCommand.orderItem.recipient ?: order.orderedBy
                shipment.addToShipmentItems(shipmentItem)
                orderItem.addToShipmentItems(shipmentItem)
            }
        }

        if (!shipment?.shipmentItems || shipment.shipmentItems.size() == 0) {
            shipment.errors.rejectValue("shipmentItems", "shipment.mustContainAtLeastOneShipmentItem.message", "Shipment must contain at least one shipment item.")
        }

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }
        if (order.hasErrors() || !order.save(flush: true)) {
            throw new ValidationException("Invalid order", order.errors)
        }

        return shipment
    }

    Shipment createInboundShipment(StockMovement stockMovement) {

        Shipment shipment = new Shipment()
        shipment.expectedShippingDate = new Date()
        shipment.name = stockMovement.generateName()
        shipment.description = stockMovement.description
        shipment.origin = stockMovement.origin
        shipment.destination = stockMovement.destination
        shipment.shipmentType = ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID)
        shipment.shipmentNumber = shipmentIdentifierService.generate(shipment)

        // Save shipment before adding the items to avoid referencing an unsaved transient instance
        shipment.save()

        stockMovement.lineItems.each { StockMovementItem stockMovementItem ->

            if (!stockMovementItem.inventoryItem) {
                stockMovementItem.inventoryItem =
                        inventoryService.findOrCreateInventoryItem(
                                stockMovementItem.product,
                                stockMovementItem?.lotNumber,
                                stockMovementItem?.expirationDate)
            }

            ShipmentItem shipmentItem = new ShipmentItem()
            shipmentItem.lotNumber = stockMovementItem.lotNumber
            shipmentItem.expirationDate = stockMovementItem.expirationDate
            shipmentItem.product = stockMovementItem.product
            shipmentItem.inventoryItem = stockMovementItem.inventoryItem
            shipmentItem.quantity = stockMovementItem.quantityRequested
            shipmentItem.sortOrder = stockMovementItem.sortOrder
            shipmentItem.recipient = stockMovementItem.recipient
            if (stockMovementItem.orderItemId) {
                OrderItem orderItem = OrderItem.get(stockMovementItem.orderItemId)
                shipmentItem.addToOrderItems(orderItem)
            }
            shipment.addToShipmentItems(shipmentItem)
        }

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        return shipment
    }

    StockMovement createRequisitionBasedStockMovement(StockMovement stockMovement) {
        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            requisition = new Requisition()
        }

        if (!requisition.status) {
            requisition.status = RequisitionStatus.CREATED
        }

        requisition.type = stockMovement.requestType
        requisition.sourceType = stockMovement.sourceType
        requisition.requisitionTemplate = stockMovement.stocklist
        requisition.description = stockMovement.description
        requisition.destination = stockMovement.destination
        requisition.origin = stockMovement.origin
        requisition.requestedBy = stockMovement.requestedBy
        requisition.dateRequested = stockMovement.dateRequested
        requisition.dateDeliveryRequested = stockMovement.dateDeliveryRequested
        requisition.name = stockMovement.generateName()
        requisition.requisitionItems = []
        requisition.approvers = stockMovement.approvers

        // Generate identifier if one has not been provided
        if (!stockMovement.identifier && !requisition.requestNumber) {
            requisition.requestNumber = requisitionIdentifierService.generate(requisition)
        }

        stockMovement.lineItems.each { stockMovementItem ->
            RequisitionItem requisitionItem = RequisitionItem.createFromStockMovementItem(stockMovementItem)
            requisition.addToRequisitionItems(requisitionItem)
        }

        addStockListItemsToRequisition(stockMovement, requisition)
        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }
        StockMovement savedStockMovement = StockMovement.createFromRequisition(requisition)

        createShipment(savedStockMovement)

        return savedStockMovement
    }

    void addStockListItemsToRequisition(StockMovement stockMovement, Requisition requisition) {
        // If the user specified a stocklist then we should automatically clone it as long as there are no
        // requisition items already added to the requisition
        RequisitionItemSortByCode sortByCode = stockMovement.stocklist?.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX
        Integer orderIndex = 0
        if (stockMovement.stocklist && !requisition.requisitionItems) {
            stockMovement.stocklist."${sortByCode.methodName}".each { stocklistItem ->
                RequisitionItem requisitionItem = new RequisitionItem()
                requisitionItem.product = stocklistItem.product
                if (requisition.sourceType == RequisitionSourceType.ELECTRONIC) {
                    def quantityOnHand = productAvailabilityService.getQuantityOnHand(stocklistItem.product, requisition.destination)
                    def quantityRequested = quantityOnHand ? (stocklistItem.quantity - quantityOnHand > 0 ? stocklistItem.quantity - quantityOnHand : 0) : stocklistItem.quantity
                    requisitionItem.quantity = quantityRequested
                    requisitionItem.quantityApproved = quantityRequested
                } else {
                    requisitionItem.quantity = stocklistItem.quantity
                    requisitionItem.quantityApproved = stocklistItem.quantity
                }
                requisitionItem.orderIndex = orderIndex
                orderIndex += 100
                requisition.addToRequisitionItems(requisitionItem)
            }
        }
    }

    StockMovement updateItems(StockMovement stockMovement, boolean removeEmptyItems) {
        if (stockMovement.requisition) {
            return updateRequisitionBasedStockMovementItems(stockMovement, removeEmptyItems)
        }
        else {
            return updateShipmentBasedStockMovementItems(stockMovement)
        }
    }

    void updateInventoryItems(StockMovement stockMovement) {
        if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                inventoryService.findAndUpdateOrCreateInventoryItem(stockMovementItem.product,
                        stockMovementItem.lotNumber, stockMovementItem.expirationDate)
            }
        }
    }

    StockMovement updateShipmentBasedStockMovementItems(StockMovement stockMovement) {
        log.info "update shipment items " + (new JSONObject(stockMovement.toJson())).toString(4)
        Shipment shipment = Shipment.get(stockMovement.id)

        if (stockMovement.lineItems) {

            // Perform lookup of inventory item before we start dealing with persistence
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                if (!stockMovementItem.inventoryItem) {
                    stockMovementItem.inventoryItem =
                            inventoryService.findOrCreateInventoryItem(
                                    stockMovementItem?.product,
                                    stockMovementItem?.lotNumber,
                                    stockMovementItem?.expirationDate)

                    // There's a case where the user might change the expiration date
                    if (stockMovementItem.inventoryItem.expirationDate != stockMovementItem.expirationDate) {
                        stockMovementItem.inventoryItem.expirationDate = stockMovementItem.expirationDate
                    }
                }
            }

            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                ShipmentItem shipmentItem = findOrCreateShipmentItem(shipment, stockMovementItem.id)
                if (!stockMovementItem.quantityRequested) {
                    if (shipmentItem.invoiceItems) {
                        throw new Exception("Shipment item for product ${shipmentItem.product.productCode} has invoice associated")
                    }
                    shipment.removeFromShipmentItems(shipmentItem)
                    shipmentItem.delete(flush: true)
                } else {
                    shipmentItem.lotNumber = stockMovementItem.lotNumber
                    shipmentItem.expirationDate = stockMovementItem.expirationDate
                    shipmentItem.product = stockMovementItem.product
                    shipmentItem.inventoryItem = stockMovementItem.inventoryItem
                    shipmentItem.quantity = stockMovementItem.quantityRequested
                    shipmentItem.recipient = stockMovementItem.recipient
                    shipmentItem.sortOrder = stockMovementItem.sortOrder
                    shipmentItem.container = createOrUpdateContainer(shipment, stockMovementItem.palletName, stockMovementItem.boxName)

                    if (stockMovementItem.orderItemId) {
                        OrderItem orderItem = OrderItem.get(stockMovementItem.orderItemId)
                        shipmentItem.addToOrderItems(orderItem)
                    }
                }
                shipmentItem.save()
            }
        }

        if (shipment.hasErrors() || !shipment.save()) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        return StockMovement.createFromShipment(Shipment.get(shipment.id))
    }


    ShipmentItem findOrCreateShipmentItem(Shipment shipment, String id) {
        log.info "find or create shipment item: " + id
        ShipmentItem shipmentItem
        if (id) {
            shipmentItem = shipment.shipmentItems.find { ShipmentItem si -> si.id == id }
            log.info "Found ${shipmentItem}"
            if (!shipmentItem) {
                throw new IllegalArgumentException("Could not find shipmente item with id ${id}")
            }
        }
        if (!shipmentItem) {
            log.info "Not found, create new and adding to shipment ${shipment.id}"
            shipmentItem = new ShipmentItem()
            shipment.addToShipmentItems(shipmentItem)
        }
        return shipmentItem
    }

    private StockMovement updateRequisitionBasedStockMovementItems(StockMovement stockMovement,
                                                                   boolean removeEmptyItems) {
        Requisition requisition = Requisition.get(stockMovement.id)

        if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                RequisitionItem requisitionItem
                // Try to find a requisition item matching the stock movement item
                if (stockMovementItem.id) {
                    requisitionItem = requisition.requisitionItems.find {
                        it.id == stockMovementItem.id
                    }
                    if (!requisitionItem) {
                        throw new IllegalArgumentException("Could not find stock movement item with ID ${stockMovementItem.id}")
                    }
                }

                // If requisition item is found, we update it
                if (requisitionItem) {
                    log.info "Item updated " + requisitionItem.id
                    requisitionItem.quantityCounted = stockMovementItem.quantityCounted
                    removeShipmentAndPicklistItemsForModifiedRequisitionItem(requisitionItem)

                    // If the item has 0 or null quantity requested, remove it. We add the ability to conditionally not
                    // remove empty items because often the update is a part of a "save as draft" feature. We only want
                    // to remove empty rows when we're actually proceeding to the next step of the requisition flow.
                    if (removeEmptyItems && !stockMovementItem.quantityRequested) {
                        log.info "Item deleted " + requisitionItem.id
                        requisitionItem.undoChanges()
                        requisition.removeFromRequisitionItems(requisitionItem)
                        requisitionItem.delete(flush: true)
                    } else {
                        if (stockMovementItem.quantityRequested != requisitionItem.quantity) {
                            requisitionItem.undoChanges()
                        }

                        requisitionItem.quantity = stockMovementItem.quantityRequested
                        requisitionItem.quantityApproved = stockMovementItem.quantityRequested

                        if (stockMovementItem.product) requisitionItem.product = stockMovementItem.product
                        if (stockMovementItem.inventoryItem) requisitionItem.inventoryItem = stockMovementItem.inventoryItem
                        if (stockMovementItem.sortOrder) requisitionItem.orderIndex = stockMovementItem.sortOrder

                        requisitionItem.recipient = stockMovementItem.recipient
                        requisitionItem.palletName = stockMovementItem.palletName
                        requisitionItem.boxName = stockMovementItem.boxName
                        requisitionItem.lotNumber = stockMovementItem.lotNumber
                        requisitionItem.expirationDate = stockMovementItem.expirationDate
                        requisitionItem.comment = stockMovementItem.comments
                    }
                }
                // Otherwise we create a new one
                else {
                    log.info "Item not found"
                    if (stockMovementItem.quantityRevised) {
                        throw new IllegalArgumentException("Cannot specify quantityRevised when creating a new item")
                    }
                    requisitionItem = new RequisitionItem()
                    requisitionItem.product = stockMovementItem.product
                    requisitionItem.inventoryItem = stockMovementItem.inventoryItem
                    requisitionItem.quantity = stockMovementItem.quantityRequested
                    requisitionItem.quantityApproved = stockMovementItem.quantityRequested
                    requisitionItem.recipient = stockMovementItem.recipient
                    requisitionItem.palletName = stockMovementItem.palletName
                    requisitionItem.boxName = stockMovementItem.boxName
                    requisitionItem.lotNumber = stockMovementItem.lotNumber
                    requisitionItem.expirationDate = stockMovementItem.expirationDate
                    requisitionItem.orderIndex = stockMovementItem.sortOrder
                    requisitionItem.comment = stockMovementItem.comments
                    requisitionItem.quantityCounted = stockMovementItem.quantityCounted
                    // If it's a new item and remove empty items flag is true, we do not want to save it if its quantity is 0
                    if (removeEmptyItems && !requisitionItem.quantityApproved) {
                        return
                    }
                    requisition.addToRequisitionItems(requisitionItem)
                }
            }
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        def updatedStockMovement = StockMovement.createFromRequisition(requisition)

        if (updatedStockMovement.lineItems) {
            updatedStockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                InventoryItem inventoryItem = inventoryService.findOrCreateInventoryItem(stockMovementItem.product,
                        stockMovementItem.lotNumber, stockMovementItem.expirationDate)
                def quantity = productAvailabilityService.getQuantityOnHand(inventoryItem)
                inventoryItem.quantity = quantity
                stockMovementItem.inventoryItem = inventoryItem
            }
        }

        createMissingPicklistItems(updatedStockMovement)
        createMissingShipmentItems(updatedStockMovement)

        return updatedStockMovement
    }

    void reviseItems(StockMovement stockMovement) {
        Requisition requisition = Requisition.get(stockMovement.id)

        if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                RequisitionItem requisitionItem = requisition.requisitionItems.find {
                    it.id == stockMovementItem.id
                }

                if (!requisitionItem) {
                    throw new IllegalArgumentException("Could not find stock movement item with ID ${stockMovementItem.id}")
                }

                log.info 'Removing previous changes, picklists and shipments, if present'
                removeShipmentAndPicklistItemsForModifiedRequisitionItem(requisitionItem)
                if (requisitionItem.isChanged() || requisitionItem.isCanceled()) {
                    requisitionItem.undoChanges()
                }

                log.info "Revising quantity for ${requisitionItem.id}"
                requisitionItem.changeQuantity(
                        stockMovementItem?.quantityRevised?.intValueExact(),
                        stockMovementItem.reasonCode,
                        stockMovementItem.comments)

                requisitionItem.quantityApproved = 0
                stockMovementItem.statusCode = "CHANGED"
            }
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }
    }

    void substituteItem(StockMovementItem stockMovementItem) {
        revertItem(stockMovementItem)

        log.info "Substitute stock movement item ${stockMovementItem}"

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem
        Requisition requisition = requisitionItem.requisition

        if (stockMovementItem.substitutionItems) {
            stockMovementItem.substitutionItems?.each { subItem ->
                requisitionItem.chooseSubstitute(
                        subItem.newProduct,
                        null,
                        subItem?.newQuantity?.intValueExact(),
                        subItem.reasonCode,
                        subItem.comments,
                        subItem.sortOrder)
                requisitionItem.quantityApproved = 0
            }
        }

        requisitionItem.save()

        createMissingPicklistForStockMovementItem(StockMovementItem.createFromRequisitionItem(requisitionItem))
        createMissingShipmentItem(requisitionItem)
    }

    def revertItem(StockMovementItem stockMovementItem) {
        removeShipmentAndPicklistItemsForModifiedRequisitionItem(stockMovementItem)

        log.info "Revert the stock movement item ${stockMovementItem}"

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem
        requisitionItem.undoChanges()
        requisitionItem.quantityApproved = requisitionItem.quantity
        requisitionItem.save(flush: true)
    }

    def revertItemAndCreateMissingPicklist(StockMovementItem stockMovementItem) {
        revertItem(stockMovementItem)

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem

        createMissingPicklistForStockMovementItem(StockMovementItem.createFromRequisitionItem(requisitionItem))
        createMissingShipmentItem(requisitionItem)
    }

    def cancelItem(StockMovementItem stockMovementItem) {
        removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem

        log.debug "Item canceled " + requisitionItem.id
        requisitionItem.cancelQuantity(stockMovementItem.reasonCode, stockMovementItem.comments)
        requisitionItem.quantityApproved = 0

        requisitionItem.save()

        return StockMovementItem.createFromRequisitionItem(requisitionItem)
    }

    /**
     * Remove all requisition items for a requisition, modification and substitution items first.
     *
     * @param requisition
     */
    void removeRequisitionItems(Requisition requisition) {

        def originalRequisitionItems =
                requisition.requisitionItems.findAll { RequisitionItem requisitionItem ->
                    requisitionItem.requisitionItemType == RequisitionItemType.ORIGINAL
                }
        def otherRequisitionItems =
                requisition.requisitionItems.minus(originalRequisitionItems)

        // Remove substitutions and modifications, then remove the original requisition items
        removeRequisitionItems(otherRequisitionItems)
        removeRequisitionItems(originalRequisitionItems)
    }

    void removeRequisitionItems(Set<RequisitionItem> requisitionItems) {
        requisitionItems?.toArray()?.each { RequisitionItem requisitionItem ->
            removeRequisitionItem(requisitionItem)
        }
    }

    void removeRequisitionItem(RequisitionItem requisitionItem) {
        Requisition requisition = requisitionItem.requisition
        removeShipmentAndPicklistItemsForModifiedRequisitionItem(requisitionItem)
        requisitionItem.undoChanges()
        requisitionItem.save(flush: true)

        requisition.removeFromRequisitionItems(requisitionItem)
        requisitionItem.delete()
        requisition.save(flush: true)
    }

    void removeShipmentItem(ShipmentItem shipmentItem) {
        if (shipmentItem.invoiceItems) {
            throw new Exception("Shipment item for product ${shipmentItem.product.productCode} has invoice associated")
        }
        Shipment shipment = shipmentItem.shipment
        OrderItem orderItem = OrderItem.get(shipmentItem.orderItemId)
        if (orderItem) {
            orderItem.removeFromShipmentItems(shipmentItem)
        }
        // do not trigger refresh on shipment for combined shipment because after deleting shipment items there will
        // be no connection between shipment and order
        shipment.disableRefresh = true
        shipment.removeFromShipmentItems(shipmentItem)
        shipmentItem.delete()
    }

    void removeShipmentItems(Set<ShipmentItem> shipmentItems) {
        shipmentItems?.toArray()?.each {shipmentItem ->
            removeShipmentItem(shipmentItem)
        }
    }

    void removeShipmentItems(Shipment shipment) {
        removeShipmentItems(shipment.shipmentItems)
    }

    void removeShipmentItemsForModifiedRequisitionItem(StockMovementItem stockMovementItem) {
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem?.id)
        removeShipmentItemsForModifiedRequisitionItem(requisitionItem)
    }

    void removeShipmentAndPicklistItemsForModifiedRequisitionItem(StockMovementItem stockMovementItem) {
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem?.id)
        removeShipmentAndPicklistItemsForModifiedRequisitionItem(requisitionItem)
    }

    void removeShipmentItemsForModifiedRequisitionItem(RequisitionItem requisitionItem) {

        // Get all shipment items associated with the given requisition item
        List<ShipmentItem> shipmentItems = ShipmentItem.findAllByRequisitionItem(requisitionItem)

        // Get all shipment items associated with the given requisition item's children
        requisitionItem?.requisitionItems?.each { RequisitionItem item ->
            shipmentItems.addAll(ShipmentItem.findAllByRequisitionItem(item))
        }

        // Delete all shipment items
        shipmentItems.each { ShipmentItem shipmentItem ->
            Shipment shipment = shipmentItem?.shipment
            // Remove shipment item from shipment to avoid re-saving by cascade after deleting item
            shipment?.removeFromShipmentItems(shipmentItem)
            shipmentItem.delete()
        }
    }

    void removeShipmentAndPicklistItemsForModifiedRequisitionItem(RequisitionItem requisitionItem) {
        if (requisitionItem?.requisition?.status < RequisitionStatus.PICKING) {
            return
        }

        removeShipmentItemsForModifiedRequisitionItem(requisitionItem)

        // Find all products for which qty ATP needs to be recalculated
        def productsToRefresh = []
        def binLocations = []
        productsToRefresh.add(requisitionItem?.product?.id)

        // Find all picklist items associated with the given requisition item
        List<PicklistItem> picklistItems = PicklistItem.findAllByRequisitionItem(requisitionItem)

        // Find all picklist items associated with the given requisition item's children
        requisitionItem?.requisitionItems?.each { RequisitionItem item ->
            picklistItems.addAll(PicklistItem.findAllByRequisitionItem(item))
            productsToRefresh.add(item?.product?.id)
        }

        picklistItems.each { PicklistItem picklistItem ->
            picklistItem.disableRefresh = Boolean.TRUE
            picklistItem.picklist?.removeFromPicklistItems(picklistItem)
            picklistItem.requisitionItem?.removeFromPicklistItems(picklistItem)
            binLocations.add(picklistItem?.binLocation?.id)
            picklistItem.delete()
        }

        // Save requisition item before PA refresh
        requisitionItem.save(flush: true)

        productAvailabilityService.refreshProductsAvailability(requisitionItem?.requisition?.origin?.id, productsToRefresh, binLocations?.unique(), false)
    }

    void updateAdjustedItems(StockMovement stockMovement, String adjustedProductCode) {
        stockMovement?.lineItems?.each { StockMovementItem stockMovementItem ->
            if (stockMovementItem.productCode == adjustedProductCode) {
                removeShipmentAndPicklistItemsForModifiedRequisitionItem(stockMovementItem.requisitionItem)
                createPicklist(stockMovementItem, false)
                createMissingShipmentItem(stockMovementItem.requisitionItem)
            }
        }
    }

    // TODO: Refactor - Move entire shipment logic to the shipmentService
    Shipment createShipment(StockMovement stockMovement) {
        log.info "create shipment " + (new JSONObject(stockMovement.toJson())).toString(4)

        Requisition requisition = stockMovement.requisition

        validateRequisition(requisition)

        Shipment shipment = Shipment.findByRequisition(requisition)

        if (!shipment) {
            shipment = new Shipment()
        } else {
            createMissingShipmentItems(stockMovement.requisition, shipment)
            return shipment
        }

        shipment.requisition = stockMovement.requisition
        shipment.shipmentNumber = stockMovement.identifier

        shipment.origin = stockMovement.origin
        shipment.destination = stockMovement.destination
        shipment.description = stockMovement.description

        // These values need defaults since they are not set until step 6
        shipment.expectedShippingDate = new Date()

        // Set default shipment type so we can save to the database without user input
        shipment.shipmentType = ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID)

        shipment.name = stockMovement.generateName()

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        return shipment
    }

    Shipment updateShipment(StockMovement stockMovement) {
        if (stockMovement.requisition) {
            return updateShipmentForRequisitionBasedStockMovement(stockMovement)
        }
        else {
            return updateShipmentForShipmentBasedStockMovement(stockMovement)
        }


    }

    Shipment updateShipmentForShipmentBasedStockMovement(StockMovement stockMovement) {
        log.info "update inbound shipment " + (new JSONObject(stockMovement.toJson())).toString(4)
        Shipment shipment = Shipment.get(stockMovement.id)
        if (!shipment) {
            throw new IllegalArgumentException("Could not find shipment for stock movement with ID ${stockMovement.id}")
        }

        if (stockMovement.statusCode == StockMovementStatusCode.DISPATCHED.toString()) {
            String inventoryLocationName = locationService.getReceivingLocationName(stockMovement.identifier)
            Location inventoryLocation = locationService.findInternalLocation(shipment.destination, inventoryLocationName)
            if (inventoryLocation != null) {
                if (stockMovement.currentStatus == ShipmentStatusCode.PARTIALLY_RECEIVED.toString()) {
                    throw new IllegalArgumentException("You can not change destination if shipment is partially received!")
                }
                if (stockMovement.destination.organization == null) {
                    throw new IllegalArgumentException("This destination does not have organization assigned")
                }
                inventoryLocation.parentLocation = stockMovement.destination
                inventoryLocation.save(flush: true, failOnError: true)
            }
        }

        shipment.additionalInformation = stockMovement.comments
        shipment.shipmentType = stockMovement.shipmentType
        shipment.driverName = stockMovement.driverName
        shipment.expectedDeliveryDate = stockMovement.expectedDeliveryDate
        shipment.expectedShippingDate = stockMovement.dateShipped
        if (stockMovement.comments) {
            shipment.addToComments(new Comment(comment: stockMovement.comments))
        }
        if (shipment.destination != stockMovement.destination) {
            shipment.name = stockMovement.generateName()
            shipment.destination = stockMovement.destination
        }

        shipmentService.createOrUpdateTrackingNumber(shipment, stockMovement.trackingNumber)
        shipment.save()
    }

    Shipment updateShipmentForRequisitionBasedStockMovement(StockMovement stockMovement) {
        log.info "update outbound shipment " + (new JSONObject(stockMovement.toJson())).toString(4)

        Shipment shipment = Shipment.findByRequisition(stockMovement.requisition)

        if (!shipment) {
            throw new IllegalArgumentException("Could not find shipment for stock movement with ID ${stockMovement.id}")
        }

        if (stockMovement.statusCode == StockMovementStatusCode.DISPATCHED.toString()) {
            String inventoryLocationName = locationService.getReceivingLocationName(stockMovement.identifier)
            Location inventoryLocation = locationService.findInternalLocation(shipment.destination, inventoryLocationName)
            if (inventoryLocation != null) {
                if (stockMovement.currentStatus == ShipmentStatusCode.PARTIALLY_RECEIVED.toString()) {
                    throw new IllegalArgumentException("You can not change destination if shipment is partially received!")
                }
                if (stockMovement.destination.organization == null) {
                    throw new IllegalArgumentException("This destination does not have organization assigned")
                }
                inventoryLocation.parentLocation = stockMovement.destination
                inventoryLocation.save(flush: true, failOnError: true)
            }
        }

        if (stockMovement.requisition.status == RequisitionStatus.ISSUED) {
            shipment.name = shipment.description == stockMovement.description && shipment.destination == stockMovement.destination ? stockMovement.name : stockMovement.generateName()

            if (shipment.destination != stockMovement.destination) {
                shipment.outgoingTransactions?.each { transaction ->
                    transaction.destination = stockMovement.destination
                    transaction.save()
                }
            }
        } else {
            shipment.name = stockMovement.generateName()
        }

        shipment.origin = stockMovement.origin
        shipment.destination = stockMovement.destination
        shipment.description = stockMovement.description
        shipment.additionalInformation = stockMovement.comments
        shipment.driverName = stockMovement.driverName
        shipment.expectedShippingDate = stockMovement.dateShipped ?: shipment.expectedShippingDate
        shipment.expectedDeliveryDate = stockMovement.expectedDeliveryDate ?: shipment.expectedDeliveryDate
        shipment.shipmentType = stockMovement.shipmentType ?: shipment.shipmentType

        shipmentService.createOrUpdateTrackingNumber(shipment, stockMovement.trackingNumber)

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        updateRequisitionOnShipmentChange(stockMovement)

        return shipment
    }


    Shipment updateShipmentOnRequisitionChange(StockMovement stockMovement) {
        Shipment shipment = Shipment.findByRequisition(stockMovement.requisition)

        if (!shipment) {
            throw new IllegalArgumentException("Could not find shipment (${stockMovement?.requisition?.id}) for stock movement with ID ${stockMovement.id}")
        }

        shipment.origin = stockMovement.origin
        shipment.destination = stockMovement.destination
        shipment.description = stockMovement.description
        shipment.name = stockMovement.generateName()

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        return shipment
    }

    ShipmentItem createOrUpdateShipmentItem(RequisitionItem requisitionItem) {

        ShipmentItem shipmentItem = ShipmentItem.findByRequisitionItem(requisitionItem)

        if (!shipmentItem) {
            shipmentItem = new ShipmentItem()
        }

        InventoryItem inventoryItem = inventoryService.findOrCreateInventoryItem(requisitionItem.product,
                requisitionItem.lotNumber, requisitionItem.expirationDate)

        shipmentItem.requisitionItem = requisitionItem
        shipmentItem.product = requisitionItem.product
        shipmentItem.inventoryItem = inventoryItem
        shipmentItem.lotNumber = inventoryItem.lotNumber
        shipmentItem.expirationDate = inventoryItem.expirationDate
        shipmentItem.quantity = requisitionItem.quantity
        shipmentItem.recipient = requisitionItem.recipient
        shipmentItem.sortOrder = requisitionItem.orderIndex
        return shipmentItem
    }


    Container createOrUpdateContainer(Shipment shipment, String palletName, String boxName) {
        if (boxName && !palletName) {
            throw new IllegalArgumentException("Please enter Pack level 1 before Pack level 2. A box must be contained within a pallet")
        }

        Container pallet = (palletName) ? shipment.findOrCreatePallet(palletName) : null
        if (pallet) {
            pallet.save(flush: true)
        }
        Container box = (boxName) ? pallet.findOrCreateBox(boxName) : null
        return box ?: pallet ?: null
    }

    void createMissingShipmentItems(StockMovement stockMovement) {
        // since there is possibility that requisition will be updated before the end of the transaction
        // where changes will not yet be persisted, we want to refresh the requisition to get the latest version
        Requisition requisition = stockMovement.requisition.refresh()

        if (requisition) {
            Shipment shipment = Shipment.findByRequisition(requisition)
            if (shipment && requisition.status >= RequisitionStatus.PICKED) {
                createMissingShipmentItems(requisition, shipment)

                if (shipment.hasErrors() || !shipment.save(flush: true)) {
                    throw new ValidationException("Invalid shipment", shipment.errors)
                }
            }
        }
    }

    void createMissingShipmentItems(Requisition requisition, Shipment shipment) {
        if (requisition.origin.isSupplier() || !requisition.origin.supports(ActivityCode.MANAGE_INVENTORY)) {
            requisition.requisitionItems?.each { RequisitionItem requisitionItem ->
                Container container = createOrUpdateContainer(shipment, requisitionItem.palletName, requisitionItem.boxName)
                ShipmentItem shipmentItem = createOrUpdateShipmentItem(requisitionItem)
                shipmentItem.container = container
                shipment.addToShipmentItems(shipmentItem)
                shipment.save(flush: true)
            }
        } else {
            requisition.requisitionItems?.each { RequisitionItem requisitionItem ->
                List<ShipmentItem> shipmentItems = createShipmentItems(requisitionItem)

                shipmentItems.each { ShipmentItem shipmentItem ->
                    shipment.addToShipmentItems(shipmentItem)
                }
                shipment.save(flush: true)
            }
        }
    }

    void createMissingShipmentItem(RequisitionItem requisitionItem) {
        Requisition requisition = requisitionItem.requisition

        if (requisition) {
            Shipment shipment = Shipment.findByRequisition(requisition)

            if (shipment && requisition.status >= RequisitionStatus.PICKED) {
                List<ShipmentItem> shipmentItems = createShipmentItems(requisitionItem)

                shipmentItems.each { ShipmentItem shipmentItem ->
                    shipment.addToShipmentItems(shipmentItem)
                }

                if (shipment.hasErrors() || !shipment.save(flush: true)) {
                    throw new ValidationException("Invalid shipment", shipment.errors)
                }
            }
        }
    }

    List<ShipmentItem> createShipmentItems(RequisitionItem requisitionItem) {
        List<ShipmentItem> shipmentItems = new ArrayList<ShipmentItem>()

        if (ShipmentItem.findAllByRequisitionItem(requisitionItem)) {
            return shipmentItems
        }

        requisitionItem?.picklistItems?.each { PicklistItem picklistItem ->
            if (picklistItem.quantity > 0) {
                ShipmentItem shipmentItem = new ShipmentItem()
                shipmentItem.lotNumber = picklistItem?.inventoryItem?.lotNumber
                shipmentItem.expirationDate = picklistItem?.inventoryItem?.expirationDate
                shipmentItem.product = picklistItem?.inventoryItem?.product
                shipmentItem.quantity = picklistItem?.quantity
                shipmentItem.requisitionItem = picklistItem.requisitionItem
                shipmentItem.recipient = picklistItem?.requisitionItem?.recipient ?:
                        picklistItem?.requisitionItem?.parentRequisitionItem?.recipient
                shipmentItem.inventoryItem = picklistItem?.inventoryItem
                shipmentItem.binLocation = picklistItem?.binLocation
                shipmentItem.sortOrder = shipmentItems.size()

                shipmentItems.add(shipmentItem)
            }
        }

        requisitionItem?.requisitionItems?.each { item ->
            shipmentItems.addAll(createShipmentItems(item))
        }

        return shipmentItems
    }

    List<PackPageItem> updatePackPageItems(List<PackPageItem> packPageItems) {
        if (packPageItems) {
            packPageItems.each { PackPageItem packPageItem ->
                updateShipmentItemAndProcessSplitLines(packPageItem)
            }
        }

        return packPageItems
    }

    void updateShipmentItemAndProcessSplitLines(PackPageItem packPageItem) {
        ShipmentItem shipmentItem = ShipmentItem.get(packPageItem?.shipmentItemId)

        if (packPageItem?.splitLineItems && shipmentItem) {
            PackPageItem item = packPageItem.splitLineItems.pop()
            shipmentItem.quantity = item?.quantityShipped
            shipmentItem.recipient = item?.recipient
            shipmentItem.container = createOrUpdateContainer(shipmentItem.shipment, item?.palletName, item?.boxName)
            shipmentItem.save(flush: true)

            for (PackPageItem splitLineItem : packPageItem.splitLineItems) {
                ShipmentItem splitItem = new ShipmentItem()
                splitItem.requisitionItem = shipmentItem.requisitionItem
                splitItem.shipment = shipmentItem.shipment
                splitItem.product = shipmentItem.product
                splitItem.lotNumber = shipmentItem.lotNumber
                splitItem.expirationDate = shipmentItem.expirationDate
                splitItem.binLocation = shipmentItem.binLocation
                splitItem.inventoryItem = shipmentItem.inventoryItem
                splitItem.sortOrder = shipmentItem.sortOrder

                splitItem.quantity = splitLineItem?.quantityShipped
                splitItem.recipient = splitLineItem?.recipient
                splitItem.container = createOrUpdateContainer(shipmentItem.shipment, splitLineItem?.palletName, splitLineItem?.boxName)

                splitItem.shipment.addToShipmentItems(splitItem)
                splitItem.save(flush: true)
            }
        } else if (shipmentItem) {
            shipmentItem.quantity = packPageItem?.quantityShipped
            shipmentItem.recipient = packPageItem?.recipient
            shipmentItem.container = createOrUpdateContainer(shipmentItem.shipment, packPageItem?.palletName, packPageItem?.boxName)
            shipmentItem.save(flush: true)
        }
    }

    void issueShipmentBasedStockMovement(String id) {
        User user = authService.currentUser
        StockMovement stockMovement = getStockMovement(id)
        Shipment shipment = stockMovement.shipment
        if (!shipment) {
            throw new IllegalStateException("There are no shipments associated with stock movement ${stockMovement.id}")
        }
        shipmentService.sendShipment(shipment, "Sent on ${new Date()}", user, shipment.origin, stockMovement.dateShipped ?: new Date())
    }

    void issueRequisitionBasedStockMovement(String id) {

        User user = authService.currentUser
        StockMovement stockMovement = getStockMovement(id)
        Requisition requisition = stockMovement.requisition
        def shipment = requisition.shipment

        validateRequisition(requisition)

        if (!shipment) {
            throw new IllegalStateException("There are no shipments associated with stock movement ${requisition.requestNumber}")
        }

        shipmentService.sendShipment(shipment, null, user, requisition.origin, stockMovement.dateShipped ?: new Date())
    }

    void validateRequisition(Requisition requisition) {

        requisition.requisitionItems.each { requisitionItem ->
            if (!requisition.origin.isSupplier() && requisition.origin.supports(ActivityCode.MANAGE_INVENTORY) && requisition.status > RequisitionStatus.CREATED) {
                validateRequisitionItem(requisitionItem)
            }
        }
    }

    void validateRequisitionItem(RequisitionItem requisitionItem) {
        // check if there is picklist created for each item that has status different than canceled, substituted or changed
        if (!requisitionItem.picklistItems && !(requisitionItem.status in [RequisitionItemStatus.CANCELED, RequisitionItemStatus.SUBSTITUTED, RequisitionItemStatus.CHANGED])) {
            throw new ValidationException("There is picklist missing for item " + requisitionItem.product.productCode + " " + requisitionItem.product.name, requisitionItem.errors)
        } else if (requisitionItem.picklistItems) {
            // if there is picklist created check if quantity picked is equal to quantity requested if there was no reason code given(items canceled during pick or picked partially have reason code)
            if (requisitionItem.totalQuantityPicked() != requisitionItem.quantity && !requisitionItem.picklistItems.reasonCode) {
                throw new ValidationException("Please change the pick qty for item " + requisitionItem.product.productCode + " " + requisitionItem.product.name + " or enter reason code.", requisitionItem.errors)
            }
        }
    }


    void rollbackStockMovement(String id) {
        StockMovement stockMovement = getStockMovement(id)

        // If the shipment has been shipped we can roll it back
        Requisition requisition = stockMovement?.requisition
        Shipment shipment = stockMovement?.requisition?.shipment ?: stockMovement?.shipment
        if (shipment && shipment.currentStatus > ShipmentStatusCode.PENDING) {
            shipmentService.rollbackLastEvent(shipment)
            if (requisition) {
                requisitionService.rollbackRequisition(requisition)
            }
        }
    }

    void synchronizeStockMovement(String id, Date dateShipped) {

        StockMovement stockMovement = getStockMovement(id)

        // Legacy requisition that needs a shipment
        Requisition requisition = stockMovement.requisition
        Shipment shipment = stockMovement?.requisition?.shipment ?: stockMovement?.shipment
        Transaction outboundTransaction = stockMovement.requisition.transactions.find { it.transactionType?.transactionCode == TransactionCode.DEBIT }
        if (requisition && outboundTransaction && !stockMovement?.shipment) {
            shipment = createShipment(stockMovement)
            shipment.expectedShippingDate = dateShipped
            createMissingShipmentItems(requisition, shipment)
            shipmentService.createShipmentEvent(shipment, dateShipped, EventCode.SHIPPED, stockMovement.origin)
            outboundTransaction.outgoingShipment = shipment
            return
        }
        // Outbound stock movement created through workflow
        else {
            // Otherwise we have a stock movement likely with an empty shipment and transaction
            if (shipment?.outgoingTransactions?.size() > 1) {
                throw new IllegalStateException("Cannot synchronize a stock movement that has more than 1 transactions")
            }

            if (!shipment) {
                shipment = createShipment(stockMovement)
                shipment.expectedShippingDate = dateShipped
            }
            createMissingShipmentItems(stockMovement)

            if (!shipment.hasShipped()) {
                shipmentService.createShipmentEvent(shipment, dateShipped, EventCode.SHIPPED, stockMovement.origin)
            }

            outboundTransaction = shipment.outgoingTransactions ?
                    shipment.outgoingTransactions.iterator().next() : null
            if (outboundTransaction) {
                shipmentService.updateOutboundTransaction(outboundTransaction, shipment)
            } else {
                shipmentService.createOutboundTransaction(shipment)
            }
        }
    }

    Boolean isSynchronizationAuthorized(StockMovement stockMovement) {
        if (!stockMovement?.requisition) {
            throw new IllegalStateException("Stock movement ${stockMovement?.id} must be an outbound stock movement")
        }
        if(stockMovement.requisition?.status != RequisitionStatus.ISSUED) {
            throw new IllegalStateException("Stock movement ${stockMovement?.id} has not been issued")
        }
        if (stockMovement?.requisition?.picklist?.picklistItems?.size() <= 0) {
            throw new IllegalStateException("Stock movement ${stockMovement?.id} must have a picklist with more than 1 item")
        }
        if (stockMovement?.shipment?.shipmentItems?.size() > 0) {
            throw new IllegalStateException("Stock movement ${stockMovement?.id} must not have any shipment items")
        }
        if (stockMovement?.shipment?.outgoingTransactions?.transactionEntries?.flatten()?.size() > 0) {
            throw new IllegalStateException("Stock movement ${stockMovement?.id} must not have any transaction entries")
        }
        return true
    }


    List<Map> getDocuments(def stockMovement) {
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        def documentList = []

        if (stockMovement?.requisition) {
            documentList.addAll([
                    [
                            name        : g.message(code: "stockMovement.exportStockMovementItems.label", default: "Export Stock Movement Items"),
                            documentType: DocumentGroupCode.EXPORT.name(),
                            contentType : "text/csv",
                            stepNumber  : 2,
                            uri         : g.createLink(controller: 'stockMovement', action: "exportCsv", id: stockMovement?.requisition?.id, absolute: true),
                            hidden      : false
                    ],
                    [
                            name        : g.message(code: "picklist.button.print.label"),
                            documentType: DocumentGroupCode.PICKLIST.name(),
                            contentType : "text/html",
                            stepNumber  : 4,
                            uri         : g.createLink(controller: 'picklist', action: "print", id: stockMovement?.requisition?.id, absolute: true)
                    ],
                    [
                            name        : g.message(code: "picklist.button.download.label"),
                            documentType: DocumentGroupCode.PICKLIST.name(),
                            contentType : "application/pdf",
                            stepNumber  : 4,
                            uri         : g.createLink(controller: 'picklist', action: "renderPdf", id: stockMovement?.requisition?.id, absolute: true),
                            hidden      : true
                    ],
                    [
                            name        : g.message(code: "picklist.button.printSortedByBins.label"),
                            documentType: DocumentGroupCode.PICKLIST.name(),
                            contentType : "text/html",
                            stepNumber  : null,
                            uri         : g.createLink(controller: 'picklist', action: "print", id: stockMovement?.requisition?.id, absolute: true, params: [sorted: true])
                    ]
            ])

            List<Document> requisitionTemplates = Document.findAllByDocumentCode(DocumentCode.REQUISITION_TEMPLATE)
            requisitionTemplates?.each { Document documentTemplate ->
                documentList << [
                    name        : documentTemplate?.name,
                    documentType: documentTemplate?.documentType?.name,
                    contentType : "application/pdf",
                    stepNumber  : null,
                    uri         : documentTemplate?.fileUri ?: g.createLink(controller: "document", action: "renderRequisitionTemplate",
                        id: stockMovement?.requisition?.id, params: ["documentTemplate.id":  documentTemplate.id, format: "PDF"],
                        absolute: true, title: documentTemplate?.filename),
                    fileUri     : documentTemplate?.fileUri
                ]
            }

            if (!stockMovement?.origin?.isSupplier() && stockMovement?.origin?.supports(ActivityCode.MANAGE_INVENTORY)) {
                documentList.add([
                        name           : g.message(code: "requisition.deliveryNote.label", default: "Delivery Note"),
                        documentType   : DocumentGroupCode.DELIVERY_NOTE.name(),
                        contentType    : "text/html",
                        stepNumber     : 5,
                        // We provide multiple sorting options for the delivery note as different download options so we
                        // need to return a list of uris. The singular 'uri' is maintained for backwards compatability.
                        uri            : g.createLink(controller: 'deliveryNote', action: "print", id: stockMovement?.requisition?.id, absolute: true),
                        downloadOptions: [
                                [
                                        name: g.message(code: "requisition.deliveryNote.sortOrder.orderIndex.label", default: "Delivery Note (shipment order)"),
                                        uri: g.createLink(
                                                controller: 'deliveryNote',
                                                action: "print",
                                                id: stockMovement?.requisition?.id,
                                                absolute: true,
                                        ),
                                ],
                                [
                                        name: g.message(code: "requisition.deliveryNote.sortOrder.product.label", default: "Delivery Note (alphabetical by product)"),
                                        uri: g.createLink(
                                                controller: 'deliveryNote',
                                                action: "print",
                                                id: stockMovement?.requisition?.id,
                                                params: [sortOrder: 'PRODUCT'],
                                                absolute: true,
                                        ),
                                ],
                        ],
                ])
            }
        }

        if (stockMovement?.shipment) {
            documentList.addAll([
                    [
                            name        : g.message(code: "shipping.exportPackingList.label"),
                            documentType: DocumentGroupCode.PACKING_LIST.name(),
                            contentType : "application/vnd.ms-excel",
                            stepNumber  : 5,
                            uri         : g.createLink(controller: 'shipment', action: "exportPackingList", id: stockMovement?.shipment?.id, absolute: true),
                            hidden      : false
                    ],
                    [
                            name        : g.message(code: "shipping.downloadPackingList.label"),
                            documentType: DocumentGroupCode.PACKING_LIST.name(),
                            contentType : "application/vnd.ms-excel",
                            stepNumber  : 5,
                            uri         : g.createLink(controller: 'doc4j', action: "downloadPackingList", id: stockMovement?.shipment?.id, absolute: true)
                    ],
                    [
                            name        : g.message(code: "shipping.downloadCertificateOfDonation.label"),
                            documentType: DocumentGroupCode.CERTIFICATE_OF_DONATION.name(),
                            contentType : "application/vnd.ms-excel",
                            stepNumber  : 5,
                            uri         : g.createLink(controller: 'doc4j', action: "downloadCertificateOfDonation", id: stockMovement?.shipment?.id, absolute: true)
                    ],
                    [
                            name        : g.message(code: "goodsReceiptNote.label"),
                            documentType: DocumentGroupCode.GOODS_RECEIPT_NOTE.name(),
                            contentType : "text/html",
                            stepNumber  : null,
                            uri         : g.createLink(controller: 'goodsReceiptNote', action: "print", id: stockMovement?.shipment?.id, absolute: true),
                            hidden      : !stockMovement?.shipment?.receipt
                    ]
            ])
        }

        if (stockMovement?.shipment) {
            ShipmentWorkflow shipmentWorkflow = shipmentService.getShipmentWorkflow(stockMovement?.shipment)
            log.info "Shipment workflow " + shipmentWorkflow
            if (shipmentWorkflow) {
                shipmentWorkflow.documentTemplates.each { Document documentTemplate ->
                    def action = getActionByDocumentCode(documentTemplate.documentType?.documentCode)
                    def isInvoiceTemplate = documentTemplate.documentType?.documentCode == DocumentCode.INVOICE_TEMPLATE
                    documentList << [
                            name        : documentTemplate?.name,
                            documentType: documentTemplate?.documentType?.name,
                            contentType : documentTemplate?.contentType,
                            stepNumber  : isInvoiceTemplate ? 5 : null,
                            uri         : documentTemplate?.fileUri ?: g.createLink(controller: 'document', action: action,
                                    id: documentTemplate?.id, params: [shipmentId: stockMovement?.shipment?.id],
                                    absolute: true, title: documentTemplate?.filename),
                            fileUri     : documentTemplate?.fileUri
                    ]
                }
            }

            stockMovement?.shipment?.documents.each { Document document ->
                def action = getActionByDocumentCode(document.documentType?.documentCode)
                documentList << [
                        id          : document?.id,
                        name        : document?.name,
                        documentType: document?.documentType?.name,
                        contentType : document?.contentType,
                        stepNumber  : null,
                        uri         : document?.fileUri ?: g.createLink(controller: 'document', action: action,
                                id: document?.id, params: [shipmentId: stockMovement?.shipment?.id],
                                absolute: true, title: document?.filename),
                        fileUri    : document?.fileUri
                ]
            }

        }

        return documentList
    }

    String getActionByDocumentCode(DocumentCode documentCode) {
        String action = ""
        switch (documentCode) {
            case DocumentCode.INVOICE_TEMPLATE:
                action = "renderInvoiceTemplate"
                break
            case DocumentCode.SHIPPING_TEMPLATE:
                action = "render"
                break
            default:
                action = "download"
        }
        return action
    }

    List buildStockMovementItemList(StockMovement stockMovement) {

        // We need to create at least one row to ensure an empty template
        if (stockMovement?.lineItems?.empty) {
            stockMovement?.lineItems.add(new StockMovementItem())
        }

        def lineItems = stockMovement.lineItems.collect {
            StockMovement.buildCsvRow(it)
        }
        return lineItems
    }

    void addDocument(MultipartFile fileContents, StockMovement stockMovement) {
        Shipment shipment = stockMovement.shipment
        Document document = new Document()
        document.fileContents = fileContents.bytes
        document.contentType = fileContents.contentType
        document.name = fileContents.originalFilename
        document.filename = fileContents.originalFilename
        document.documentType = DocumentType.get(Constants.DEFAULT_DOCUMENT_TYPE_ID)

        shipment.addToDocuments(document)
    }

    void saveDocument(MultipartFile fileContent, StockMovement stockMovement) {
        addDocument(fileContent, stockMovement)
        stockMovement.shipment.save()
    }

    void saveDocuments(List<MultipartFile> filesContents, StockMovement stockMovement) {
        filesContents.each { fileContent ->
            addDocument(fileContent, stockMovement)
        }
        stockMovement.shipment.save()
    }

    def getDisabledMessage(StockMovement stockMovement, Location currentLocation, Boolean isEditing = false) {
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')

        boolean isSameOrigin = stockMovement?.origin?.id == currentLocation?.id
        boolean isSameDestination = stockMovement?.destination?.id == currentLocation?.id
        boolean isDepot = stockMovement?.origin?.isDepot()

        if ((stockMovement?.hasBeenReceived() || stockMovement?.hasBeenPartiallyReceived()) && isEditing) {
            return g.message(code: "stockMovement.cantEditReceived.message")
        } else if (!isSameOrigin && isDepot && stockMovement?.isPending() && !stockMovement?.isElectronicType()
         || (!isDepot && !isSameDestination && isEditing)) {
            return g.message(code: "stockMovement.isDifferentOrigin.message")
        } else if (stockMovement?.hasBeenReceived()) {
            return g.message(code: "stockMovement.hasAlreadyBeenReceived.message", args: [stockMovement?.identifier])
        } else if (!(stockMovement?.hasBeenShipped() || stockMovement?.hasBeenPartiallyReceived())) {
            return g.message(code: "stockMovement.hasNotBeenShipped.message", args: [stockMovement?.identifier])
        } else if (!isSameDestination) {
            return g.message(code: "stockMovement.isDifferentLocation.message")
        } else if (!stockMovement?.hasBeenIssued() && !stockMovement?.isFromOrder) {
            return g.message(code: "stockMovement.hasNotBeenIssued.message", args: [stockMovement?.identifier])
        }
    }

    Boolean validatePicklist(String stockMovementId) {
        StockMovement stockMovement = getStockMovement(stockMovementId)
        validatePicklist(stockMovement)
    }

    Boolean validatePicklist(StockMovement stockMovement) {
        if (stockMovement?.requisition?.picklist) {
            Shipment shipment = Shipment.findByRequisition(stockMovement?.requisition)
            shipmentService.validateShipment(shipment)
            validateQuantityRequested(stockMovement)
        }
        return true
    }

    void rollbackApproval(String stockMovementId) {
        StockMovement stockMovement = getStockMovement(stockMovementId)
        Location currentLocation = AuthService.currentLocation
        if (!canRollbackApproval(currentLocation, AuthService.currentUser, stockMovement)) {
            String errorMessage = applicationTagLib.message(
                    code: "request.rollbackApproval.insufficientPermissions.message",
                    default: "Unable to rollback approval due to insufficient permissions",
            )
            throw new IllegalAccessException(errorMessage)
        }

        Requisition requisition = stockMovement?.requisition
        if (requisition.status in [RequisitionStatus.APPROVED, RequisitionStatus.REJECTED]) {
            requisitionService.rollbackLastEvent(requisition)
            requisition.status = RequisitionStatus.PENDING_APPROVAL
            requisition.approvedBy = null
            requisition.rejectedBy = null
            requisition.dateApproved = null
            requisition.dateRejected = null
        }
    }

    Boolean canRollbackApproval(Location location, User user, StockMovement stockMovement) {
        return (user.hasRoles(location, [RoleType.ROLE_REQUISITION_APPROVER]) ||
                userService.isUserAdmin(user) ||
                user?.id == stockMovement?.requestedBy?.id) &&
                stockMovement.isInApprovalState()
    }

    void deleteComment(Comment comment, StockMovement stockMovement) {
        Event event = Event.findByComment(comment)
        if (event) {
            event.comment = null
        }
        def associatedObject = stockMovement?.requisition ?: stockMovement?.shipment
        associatedObject?.removeFromComments(comment)
    }

    Comment saveComment(Comment comment, StockMovement stockMovement) {
        def associatedObject  = stockMovement?.requisition ?: stockMovement?.shipment
        if (!comment.id) {
            associatedObject?.addToComments(comment)
        }
        return comment.save()
    }

    ApplicationTagLib getApplicationTagLib() {
        return grailsApplication.mainContext.getBean(ApplicationTagLib)
    }
}
