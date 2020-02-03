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

import grails.orm.PagedResultList
import grails.validation.ValidationException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.DocumentGroupCode
import org.pih.warehouse.api.EditPage
import org.pih.warehouse.api.EditPageItem
import org.pih.warehouse.api.PackPage
import org.pih.warehouse.api.PackPageItem
import org.pih.warehouse.api.PickPage
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.SubstitutionItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemSortByCode
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.shipping.ShipmentWorkflow

class StockMovementService {

    def productService
    def identifierService
    def requisitionService
    def shipmentService
    def locationService
    def inventoryService
    def inventorySnapshotService
    def orderService

    boolean transactional = true

    def grailsApplication

    def createStockMovement(StockMovement stockMovement) {

        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }
        Requisition requisition = createRequisition(stockMovement)
        return StockMovement.createFromRequisition(requisition)
    }

    def createFromOrder(Order order) {
        StockMovement stockMovement = StockMovement.createFormOrder(order)

        Requisition requisition = createRequisition(stockMovement)
        return StockMovement.createFromRequisition(requisition)
    }

    void updateStatus(String id, RequisitionStatus status) {

        log.info "Update status ${id} " + status
        Requisition requisition = Requisition.get(id)
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
            requisition.status = status
            requisition.save(flush: true)
        }
    }


    StockMovement updateRequisition(StockMovement stockMovement) {
        log.info "Update stock movement " + new JSONObject(stockMovement.toJson()).toString(4)

        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            throw new ObjectNotFoundException(stockMovement.id, StockMovement.class.toString())
        }

        if (stockMovement.destination) requisition.destination = stockMovement.destination
        if (stockMovement.origin) requisition.origin = stockMovement.origin
        if (stockMovement.description) requisition.description = stockMovement.description
        if (stockMovement.requestedBy) requisition.requestedBy = stockMovement.requestedBy
        if (stockMovement.dateRequested) requisition.dateRequested = stockMovement.dateRequested
        requisition.name = stockMovement.generateName()

        if (requisition.requisitionTemplate?.id != stockMovement.stocklist?.id) {
            removeRequisitionItems(requisition)
            addStockListItemsToRequisition(stockMovement, requisition)
            requisition.requisitionTemplate = stockMovement.stocklist
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        if (RequisitionStatus.CHECKING == requisition.status || RequisitionStatus.PICKED == requisition.status || RequisitionStatus.ISSUED == requisition.status) {
            log.info "Updating shipment for stock movement ${stockMovement}"
            updateShipmentWhenRequisitionChanged(stockMovement)
        }

        stockMovement = StockMovement.createFromRequisition(requisition.refresh())

        createMissingPicklistItems(stockMovement)
        createMissingShipmentItems(stockMovement)

        return stockMovement
    }

    void updateRequisitionWhenShipmentChanged(StockMovement stockMovement) {
        log.info "Update stock movement " + new JSONObject(stockMovement.toJson()).toString(4)

        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            throw new ObjectNotFoundException(stockMovement.id, StockMovement.class.toString())
        }

        if (RequisitionStatus.ISSUED == requisition.status) {
            requisition.name = stockMovement.description == requisition.description && requisition.destination == stockMovement.destination ? stockMovement.name : stockMovement.generateName()
            requisition.destination = stockMovement.destination
            requisition.description = stockMovement.description

            if (requisition.hasErrors() || !requisition.save(flush: true)) {
                throw new ValidationException("Invalid requisition", requisition.errors)
            }
        }
    }

    void deleteStockMovement(String id) {
        StockMovement stockMovement = getStockMovement(id)
        if (stockMovement?.requisition) {
            stockMovement.requisition.delete()
        }
        if (stockMovement?.shipment) {
            stockMovement.shipment.delete()
        }
    }

    def getStockMovements(Integer maxResults, Integer offset) {
        return getStockMovements(new StockMovement(), [:], maxResults, offset)
    }

    def getStockMovements(Map params, Integer maxResults, Integer offset) {
        return getStockMovements(new StockMovement(), params, maxResults, offset)
    }

    def getStockMovements(StockMovement stockMovement, Map params, Integer maxResults, Integer offset) {
        log.info "Get stock movements: " + stockMovement.toJson()

        log.info "Stock movement: ${stockMovement?.shipmentStatusCode}"

        def requisitions = Requisition.createCriteria().list(max: maxResults, offset: offset) {
            eq("isTemplate", Boolean.FALSE)

            if (stockMovement?.receiptStatusCode) {
                shipments {
                    eq("currentStatus", stockMovement.receiptStatusCode)
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
            if (stockMovement.statusCode) {
                eq("status", RequisitionStatus.valueOf(stockMovement.statusCode))
            }
            if (stockMovement.requestedBy) {
                eq("requestedBy", stockMovement.requestedBy)
            }
            if (stockMovement.createdBy) {
                eq("createdBy", stockMovement.createdBy)
            }

            if (params.sort && params.order) {
                order(params.sort, params.order)
            } else {
                order("dateCreated", "desc")
            }
        }


        def stockMovements = requisitions.collect { requisition ->
            return StockMovement.createFromRequisition(requisition)
        }

        return new PagedResultList(stockMovements, requisitions.totalCount)
    }


    StockMovement getStockMovement(String id) {
        return getStockMovement(id, null)
    }

    StockMovement getStockMovement(String id, String stepNumber) {
        log.info "Getting stock movement for id ${id} step number ${stepNumber}"

        Requisition requisition = Requisition.get(id)
        if (!requisition) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }

        def startTime = System.currentTimeMillis()
        StockMovement stockMovement = StockMovement.createFromRequisition(requisition)
        log.info(">>>>>>>>>>>>>>> createFromRequisition: ${System.currentTimeMillis() - startTime} ms")

        startTime = System.currentTimeMillis()
        stockMovement.documents = getDocuments(stockMovement)
        log.info(">>>>>>>>>>>>>>> getDocuments: ${System.currentTimeMillis() - startTime} ms")

        startTime = System.currentTimeMillis()
        if (stepNumber.equals("3")) {
            stockMovement.lineItems = null
            stockMovement.editPage = getEditPage(id)
        } else if (stepNumber.equals("4")) {
            stockMovement.lineItems = null
            stockMovement.pickPage = getPickPage(id)
        } else if (stepNumber.equals("5")) {
            stockMovement.lineItems = null
            stockMovement.packPage = getPackPage(id)
        } else if (stepNumber.equals("6")) {
            if (!stockMovement.origin.isSupplier() && stockMovement.origin.supports(ActivityCode.MANAGE_INVENTORY)) {
                stockMovement.lineItems = null
                stockMovement.packPage = getPackPage(id)
            }
        }
        log.info(">>>>>>>>>>>>>>> get stock movement for stepNumber ${stepNumber}: ${System.currentTimeMillis() - startTime} ms")

        return stockMovement
    }

    StockMovementItem getStockMovementItem(String id) {
        RequisitionItem requisitionItem = RequisitionItem.get(id)
        return StockMovementItem.createFromRequisitionItem(requisitionItem)
    }


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
        log.info "Clear picklist"
        if (picklist) {
            picklist.picklistItems.findAll {
                it.requisitionItem == requisitionItem
            }.toArray().each {
                picklist.removeFromPicklistItems(it)
                requisitionItem.removeFromPicklistItems(it)
                it.delete()
            }
            picklist.save()
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
            createPicklist(stockMovementItem)
        }
    }

    /**
     * Create an automated picklist for the stock movenent associated with the given id.
     *
     * @param id
     */
    void createPicklist(String id) {
        StockMovement stockMovement = getStockMovement(id)
        createPicklist(stockMovement)
    }

    /**
     * Create an automated picklist for the given stock movement.
     *
     * @param stockMovement
     */
    void createPicklist(StockMovement stockMovement) {
        for (StockMovementItem stockMovementItem : stockMovement.lineItems) {
            if (stockMovementItem.statusCode == 'SUBSTITUTED') {
                for (StockMovementItem subStockMovementItem : stockMovementItem.substitutionItems) {
                    createPicklist(subStockMovementItem)
                }
            } else {
                createPicklist(stockMovementItem)
            }
        }
    }

    void createPicklist(StockMovementItem stockMovementItem) {
        log.info "Create picklist for stock movement item ${stockMovementItem.toJson()}"

        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        createPicklist(requisitionItem)
    }

    /**
     * Create an automated picklist for the given stock movement item.
     *
     * @param id
     */
    void createPicklist(RequisitionItem requisitionItem) {
        Product product = requisitionItem.product
        Location location = requisitionItem?.requisition?.origin
        Integer quantityRequired = requisitionItem?.calculateQuantityRequired()

        log.info "QUANTITY REQUIRED: ${quantityRequired}"

        if (quantityRequired) {
            // Retrieve all available items and then calculate suggested
            List<AvailableItem> availableItems = inventorySnapshotService.getAvailableBinLocations(location, product)
            log.info "Available items: ${availableItems}"
            List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequired)
            log.info "Suggested items " + suggestedItems
            if (suggestedItems) {
                clearPicklist(requisitionItem)
                for (SuggestedItem suggestedItem : suggestedItems) {
                    createOrUpdatePicklistItem(requisitionItem,
                            null,
                            suggestedItem.inventoryItem,
                            suggestedItem.binLocation,
                            suggestedItem.quantityPicked.intValueExact(),
                            null,
                            null)
                }
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
                                    Integer quantity, String reasonCode, String comment) {

        Requisition requisition = requisitionItem.requisition

        Picklist picklist = Picklist.findByRequisition(requisition)
        if (!picklist) {
            picklist = new Picklist()
            picklist.requisition = requisition
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
        }
        picklist.save(flush: true)
    }

    void createOrUpdatePicklistItem(StockMovement stockMovement) {

        Requisition requisition = stockMovement.requisition

        Picklist picklist = requisition?.picklist
        if (!picklist) {
            picklist = new Picklist()
            picklist.requisition = requisition
        }

        stockMovement.pickPage.pickPageItems.each { pickPageItem ->
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

        picklist.save()
    }

    /**
     * Get a list of suggested items for the given stock movement item.
     *
     * @param stockMovementItem
     * @return
     */
    List getSuggestedItems(List<AvailableItem> availableItems, Integer quantityRequested) {

        List suggestedItems = []

        // As long as quantity requested is less than the total available we can iterate through available items
        // and pick until quantity requested is 0. Otherwise, we don't suggest anything because the user must
        // choose anyway. This might be improved in the future.
        Integer quantityAvailable = availableItems ? availableItems?.sum {
            it.quantityAvailable
        } : 0
        if (quantityRequested <= quantityAvailable) {

            for (AvailableItem availableItem : availableItems) {
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

    /**
     * Get a list of substitution items for the given stock movement item.
     *
     * @param stockMovementItem
     * @return
     */
    List getSubstitutionItems(StockMovementItem stockMovementItem) {

        // Gather all substitutions
        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)

        List substitutionItems = requisitionItem?.substitutionItems?.collect { substitutionItem ->
            StockMovementItem.createFromRequisitionItem(substitutionItem)
        }
        return substitutionItems
    }


    List<SubstitutionItem> getAvailableSubstitutions(Location location, Product product) {

        List<SubstitutionItem> availableSubstitutions
        if (location) {
            def productAssociations =
                    productService.getProductAssociations(product, [ProductAssociationTypeCode.SUBSTITUTE])

            availableSubstitutions = productAssociations.collect { productAssociation ->

                def associatedProduct = productAssociation.associatedProduct
                def availableItems = getAvailableBinLocations(location, associatedProduct)

                log.info "Available items for substitution ${associatedProduct}: ${availableItems}"
                SubstitutionItem substitutionItem = new SubstitutionItem()
                substitutionItem.productId = associatedProduct.id
                substitutionItem.productName = associatedProduct.name
                substitutionItem.productCode = associatedProduct.productCode
                substitutionItem.availableItems = availableItems
                return substitutionItem
            }
        }
        return availableSubstitutions.findAll { availableItems -> availableItems.quantityAvailable > 0 }
    }

    List<SubstitutionItem> getSubstitutionItems(Location location, RequisitionItem requisitionItem) {
        !requisitionItem?.substitutionItems ? null : requisitionItem?.substitutionItems?.collect { RequisitionItem item ->
            List<AvailableItem> availableItems = getAvailableBinLocations(location, item.product)

            SubstitutionItem substitutionItem = new SubstitutionItem()
            substitutionItem.productId = item?.product?.id
            substitutionItem.productName = item?.product?.name
            substitutionItem.productCode = item?.product?.productCode
            substitutionItem.quantitySelected = item?.quantity
            substitutionItem.quantityConsumed = calculateMonthlyStockListQuantity(item.product, location)
            substitutionItem.availableItems = availableItems
            return substitutionItem
        }
    }

    List<AvailableItem> getAvailableBinLocations(Location location, Product product) {
        List availableBinLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location, [product])
        List<AvailableItem> availableItems = availableBinLocations.collect {
            return new AvailableItem(
                    inventoryItem: it?.inventoryItem,
                    binLocation: it?.binLocation,
                    quantityAvailable: it.quantity
            )
        }

        return inventoryService.sortAvailableItems(availableItems)
    }


    EditPage getEditPage(String id) {
        EditPage editPage = new EditPage()
        def startTime = System.currentTimeMillis()
        StockMovement stockMovement = getStockMovement(id)
        log.info("Get stock movement ${id}: ${System.currentTimeMillis() - startTime}")

        Map monthlyStocklistQuantities = calculateMonthlyStockListQuantity(stockMovement.origin)

        startTime = System.currentTimeMillis()
        stockMovement.lineItems.each { stockMovementItem ->
            stockMovementItem.stockMovement = stockMovement
            EditPageItem editPageItem = buildEditPageItem(stockMovementItem)
            editPageItem.quantityConsumed = monthlyStocklistQuantities.get(stockMovementItem.product.id)
            editPage.editPageItems.addAll(editPageItem)
        }
        log.info("Build edit pages for stock movement: ${System.currentTimeMillis() - startTime} ms")
        return editPage
    }


    PickPage getPickPage(String id) {
        PickPage pickPage = new PickPage()

        StockMovement stockMovement = getStockMovement(id)
        stockMovement.lineItems.each { stockMovementItem ->
            List pickPageItems = getPickPageItems(stockMovementItem)
            pickPage.pickPageItems.addAll(pickPageItems)
        }
        return pickPage
    }


    PackPage getPackPage(String id) {
        PackPage packPage = new PackPage()

        StockMovement stockMovement = getStockMovement(id)
        Set<PackPageItem> packPageItems = new LinkedHashSet<PackPageItem>()
        stockMovement.requisition?.picklist?.picklistItems?.sort { a, b ->
            a.sortOrder <=> b.sortOrder ?: a.id <=> b.id
        }?.each { PicklistItem picklistItem ->
            packPageItems.addAll(getPackPageItems(picklistItem))
        }

        packPage.packPageItems.addAll(packPageItems)
        return packPage
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
            pickPageItems = requisitionItem.substitutionItems.collect {
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

    Float calculateMonthlyStockListQuantity(Product product, Location location) {

        List monthlyStockListQuantities = RequisitionItem.createCriteria().list {
            projections {
                property("quantity")
                requisition {
                    property("replenishmentPeriod")
                }

            }
            requisition {
                eq("isTemplate", Boolean.TRUE)
                eq("isPublished", Boolean.TRUE)
                eq("origin", location)
            }
            eq("product", product)
        }

        Float monthlyStockListQuantity =
                monthlyStockListQuantities.sum {
                    it[1] ? Math.ceil(((Double) it[0]) / it[1] * 30) : 0
                }

        return monthlyStockListQuantity
    }

    def calculateMonthlyStockListQuantity(Location location) {

        List stocklistItems = RequisitionItem.createCriteria().list {
            projections {
                property("product.id")
                property("quantity")
                requisition {
                    property("replenishmentPeriod")
                }
            }
            requisition {
                eq("isTemplate", Boolean.TRUE)
                eq("isPublished", Boolean.TRUE)
                eq("origin", location)
            }
        }

        def monthlyStockListQuantities = stocklistItems.collect {
            Float quantity = it[2] ? Math.ceil(((Double) it[1]) / it[2] * 30) : 0
            [product: it[0], quantity: quantity]
        }

        // Get a map of monthly stocklist quantities with productId, quantities summed
        monthlyStockListQuantities =
                monthlyStockListQuantities.groupBy { it?.product }.
                        collect { k, v -> [productId: k, quantity: v.quantity.sum()] }

        // Rebuild list of maps as a map (productId: quantity]
        monthlyStockListQuantities =
                monthlyStockListQuantities.inject([:]) { map, col ->
                    map << [(col.productId): col.quantity]
                }

        return monthlyStockListQuantities
    }


    Float calculateMonthlyStockListQuantity(StockMovementItem stockMovementItem) {
        Integer monthlyStockListQuantity = 0
        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)
        Location location = requisitionItem?.requisition?.origin
        List<Requisition> stocklists = requisitionService.getRequisitionTemplates(location)
        if (stocklists) {
            stocklists.each { stocklist ->
                def stocklistItems = stocklist.requisitionItems.findAll {
                    it?.product?.id == requisitionItem?.product?.id
                }
                if (stocklistItems) {
                    monthlyStockListQuantity += stocklistItems.sum {
                        it?.requisition?.replenishmentPeriod ? Math.ceil(((Double) it?.quantity) / it?.requisition?.replenishmentPeriod * 30) : 0
                    }
                }
            }
        }
        return monthlyStockListQuantity
    }

    EditPageItem buildEditPageItem(StockMovementItem stockMovementItem) {
        EditPageItem editPageItem = new EditPageItem()
        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)
        Location location = requisitionItem?.requisition?.origin

        // Qty Available
        List<AvailableItem> availableItems = inventorySnapshotService.getAvailableBinLocations(location, requisitionItem.product)

        // Substitution
        List<SubstitutionItem> availableSubstitutions = getAvailableSubstitutions(location, requisitionItem.product)
        List<SubstitutionItem> substitutionItems = getSubstitutionItems(location, requisitionItem)

        editPageItem.requisitionItem = requisitionItem
        editPageItem.productId = requisitionItem.product.id
        editPageItem.productCode = requisitionItem.product.productCode
        editPageItem.productName = requisitionItem.product.name
        editPageItem.quantityRequested = requisitionItem.quantity
        editPageItem.quantityConsumed = calculateMonthlyStockListQuantity(stockMovementItem)
        editPageItem.availableSubstitutions = availableSubstitutions
        editPageItem.availableItems = availableItems
        editPageItem.substitutionItems = substitutionItems
        editPageItem.sortOrder = stockMovementItem.sortOrder
        return editPageItem
    }

    /**
     *
     * @param requisitionItem
     * @return
     */
    PickPageItem buildPickPageItem(RequisitionItem requisitionItem, Integer sortOrder) {

        if (!requisitionItem.picklistItems || (requisitionItem.picklistItems && requisitionItem.totalQuantityPicked() != requisitionItem.quantity &&
                !requisitionItem.picklistItems.reasonCode)) {
            createPicklist(requisitionItem)
        }
        PickPageItem pickPageItem = new PickPageItem(requisitionItem: requisitionItem,
                picklistItems: requisitionItem.picklistItems)
        Location location = requisitionItem?.requisition?.origin

        List<AvailableItem> availableItems = inventorySnapshotService.getAvailableBinLocations(location, requisitionItem.product)
        Integer quantityRequired = requisitionItem?.calculateQuantityRequired()
        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequired)
        pickPageItem.availableItems = availableItems
        pickPageItem.suggestedItems = suggestedItems
        pickPageItem.sortOrder = sortOrder

        return pickPageItem
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
        } else if (shipmentItem.container) {
            palletName = shipmentItem?.container?.name
        }

        return new PackPageItem(shipmentItem: shipmentItem, palletName: palletName, boxName: boxName)
    }

    Requisition createRequisition(StockMovement stockMovement) {
        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            requisition = new Requisition()
        }

        if (!requisition.status) {
            requisition.status = RequisitionStatus.CREATED
        }

        // Generate identifier if one has not been provided
        if (!stockMovement.identifier && !requisition.requestNumber) {
            requisition.requestNumber = identifierService.generateRequisitionIdentifier()
        }
        requisition.type = RequisitionType.DEFAULT
        requisition.requisitionTemplate = stockMovement.stocklist
        requisition.description = stockMovement.description
        requisition.destination = stockMovement.destination
        requisition.origin = stockMovement.origin
        requisition.requestedBy = stockMovement.requestedBy
        requisition.dateRequested = stockMovement.dateRequested
        requisition.name = stockMovement.generateName()
        requisition.requisitionItems = []

        stockMovement.lineItems.each { stockMovementItem ->
            RequisitionItem requisitionItem = RequisitionItem.createFromStockMovementItem(stockMovementItem, requisition)
            requisition.requisitionItems.add(requisitionItem)
        }

        addStockListItemsToRequisition(stockMovement, requisition)
        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }
        return requisition
    }

    void addStockListItemsToRequisition(StockMovement stockMovement, Requisition requisition) {
        // If the user specified a stocklist then we should automatically clone it as long as there are no
        // requisition items already added to the requisition
        RequisitionItemSortByCode sortByCode = stockMovement.stocklist?.sortByCode ?: RequisitionItemSortByCode.SORT_INDEX
        def orderIndex = 0

        if (stockMovement.stocklist && !requisition.requisitionItems) {
            stockMovement.stocklist."${sortByCode.methodName}".each { stocklistItem ->
                RequisitionItem requisitionItem = new RequisitionItem()
                requisitionItem.product = stocklistItem.product
                requisitionItem.quantity = stocklistItem.quantity
                requisitionItem.quantityApproved = stocklistItem.quantity
                requisitionItem.orderIndex = orderIndex
                requisition.addToRequisitionItems(requisitionItem)

                orderIndex++
            }
        }
    }

    StockMovement updateItems(StockMovement stockMovement) {
        Requisition requisition = Requisition.get(stockMovement.id)

        if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                RequisitionItem requisitionItem
                // Try to find a matching stock movement item
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

                    removeShipmentItemsForModifiedRequisitionItem(requisitionItem)

                    if (!stockMovementItem.quantityRequested) {
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
                    requisitionItem.orderItem = stockMovementItem.orderItem
                    requisition.addToRequisitionItems(requisitionItem)
                }
            }
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        stockMovement = StockMovement.createFromRequisition(requisition)

        createMissingPicklistItems(stockMovement)
        createMissingShipmentItems(stockMovement)

        return stockMovement
    }

    List<EditPageItem> reviseItems(StockMovement stockMovement) {
        Requisition requisition = Requisition.get(stockMovement.id)
        def revisedItems = []

        if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                RequisitionItem requisitionItem = requisition.requisitionItems.find {
                    it.id == stockMovementItem.id
                }

                if (!requisitionItem) {
                    throw new IllegalArgumentException("Could not find stock movement item with ID ${stockMovementItem.id}")
                }

                removeShipmentItemsForModifiedRequisitionItem(requisitionItem)

                log.info "Item revised " + requisitionItem.id

                // Cannot cancel quantity if it has already been canceled
                if (requisitionItem.quantityCanceled) {
                    requisitionItem.undoChanges()
                }

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

        createMissingPicklistItems(stockMovement)
        createMissingShipmentItems(stockMovement)

        stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
            if (stockMovementItem.statusCode == 'CHANGED') {
                EditPageItem editPageItem = buildEditPageItem(stockMovementItem)
                revisedItems.add(editPageItem)
            }
        }

        return revisedItems
    }

    void substituteItem(StockMovementItem stockMovementItem) {
        removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        log.info "Substitute stock movement item ${stockMovementItem}"

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem
        Requisition requisition = requisitionItem.requisition

        //this is for split line during substitution (if substituted item has available quantity it shows up in the substitutions list)
        if (stockMovementItem.quantityRevised) {
            Integer changedQuantity = requisitionItem.quantity - stockMovementItem.newQuantity?.intValueExact()
            requisitionItem.quantity = changedQuantity > 0 && changedQuantity < requisitionItem.quantity ? changedQuantity : requisitionItem.quantity

            RequisitionItem newItem = new RequisitionItem()
            newItem.quantity = stockMovementItem.quantityRevised
            newItem.quantityApproved = newItem.quantity
            newItem.orderIndex = stockMovementItem.sortOrder
            newItem.product = requisitionItem.product
            newItem.recipient = requisitionItem.recipient
            newItem.palletName = requisitionItem.palletName
            newItem.boxName = requisitionItem.boxName
            newItem.lotNumber = requisitionItem.lotNumber
            newItem.expirationDate = requisitionItem.expirationDate
            newItem.requisition = requisition
            newItem.save()

            requisition.addToRequisitionItems(newItem)

            createMissingPicklistForStockMovementItem(StockMovementItem.createFromRequisitionItem(newItem))
            createMissingShipmentItem(newItem)
        }

        if (stockMovementItem.substitutionItems) {
            stockMovementItem.substitutionItems?.each { subItem ->
                requisitionItem.chooseSubstitute(
                        subItem.newProduct,
                        null,
                        subItem?.newQuantity?.intValueExact(),
                        subItem.reasonCode,
                        subItem.comments)
                requisitionItem.quantityApproved = 0
            }
        }

        requisitionItem.save()

        createMissingPicklistForStockMovementItem(StockMovementItem.createFromRequisitionItem(requisitionItem))
        createMissingShipmentItem(requisitionItem)
    }

    def revertItem(StockMovementItem stockMovementItem) {
        removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        log.info "Revert the stock movement item ${stockMovementItem}"

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem
        requisitionItem.undoChanges()
        requisitionItem.quantityApproved = requisitionItem.quantity
        requisitionItem.save(flush: true)

        createMissingPicklistForStockMovementItem(StockMovementItem.createFromRequisitionItem(requisitionItem))
        createMissingShipmentItem(requisitionItem)
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
        requisitionItem.undoChanges()
        requisitionItem.save(flush: true)

        removeShipmentItemsForModifiedRequisitionItem(requisitionItem)
        requisition.removeFromRequisitionItems(requisitionItem)
        requisitionItem.delete()
    }


    void removeShipmentItemsForModifiedRequisitionItem(StockMovementItem stockMovementItem) {
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem?.id)
        removeShipmentItemsForModifiedRequisitionItem(requisitionItem)
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
            shipmentItem.delete()
        }

        // Find all picklist items associated with the given requisition item
        List<PicklistItem> picklistItems = PicklistItem.findAllByRequisitionItem(requisitionItem)

        // Find all picklist items associated with the given requisition item's children
        requisitionItem?.requisitionItems?.each { RequisitionItem item ->
            picklistItems.addAll(PicklistItem.findAllByRequisitionItem(item))
        }

        picklistItems.each { PicklistItem picklistItem ->
            picklistItem.delete()
        }
    }

    void updateAdjustedItems(StockMovement stockMovement, String adjustedProductCode) {
        stockMovement?.lineItems?.each { StockMovementItem stockMovementItem ->
            if (stockMovementItem.productCode == adjustedProductCode) {
                removeShipmentItemsForModifiedRequisitionItem(stockMovementItem.requisitionItem)
                createPicklist(stockMovementItem)
                createMissingShipmentItem(stockMovementItem.requisitionItem)
            }
        }
    }

    Shipment createShipment(StockMovement stockMovement) {
        log.info "create shipment " + (new JSONObject(stockMovement.toJson())).toString(4)

        Requisition requisition = stockMovement.requisition

        validateRequisition(requisition)

        Shipment shipment = Shipment.findByRequisition(requisition)

        if (!shipment) {
            shipment = new Shipment()
        } else {
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

        createMissingShipmentItems(stockMovement.requisition, shipment)

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        return shipment
    }

    Shipment updateShipment(StockMovement stockMovement) {
        log.info "update shipment " + (new JSONObject(stockMovement.toJson())).toString(4)

        Shipment shipment = Shipment.findByRequisition(stockMovement.requisition)

        if (!shipment) {
            throw new IllegalArgumentException("Could not find shipment for stock movement with ID ${stockMovement.id}")
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

        shipment.shipmentType = stockMovement.shipmentType ?: shipment.shipmentType

        ReferenceNumberType trackingNumberType = ReferenceNumberType.findById(Constants.TRACKING_NUMBER_TYPE_ID)
        if (!trackingNumberType) {
            throw new IllegalStateException("Must configure reference number type for Tracking Number with ID '${Constants.TRACKING_NUMBER_TYPE_ID}'")
        }

        // Needed to use ID since reference numbers is lazy loaded and equality operation was not working
        ReferenceNumber referenceNumber = shipment.referenceNumbers.find { ReferenceNumber refNum ->
            trackingNumberType?.id?.equals(refNum.referenceNumberType?.id)
        }

        if (stockMovement.trackingNumber) {
            // Create a new reference number
            if (!referenceNumber) {
                referenceNumber = new ReferenceNumber()
                referenceNumber.identifier = stockMovement.trackingNumber
                referenceNumber.referenceNumberType = trackingNumberType
                shipment.addToReferenceNumbers(referenceNumber)
            }
            // Update the existing reference number
            else {
                referenceNumber.identifier = stockMovement.trackingNumber
            }
        } else if (referenceNumber) {
            shipment.removeFromReferenceNumbers(referenceNumber)
        }

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        updateRequisitionWhenShipmentChanged(stockMovement)

        return shipment
    }

    Shipment updateShipmentWhenRequisitionChanged(StockMovement stockMovement) {
        log.info "update shipment " + (new JSONObject(stockMovement.toJson())).toString(4)

        Shipment shipment = Shipment.findByRequisition(stockMovement.requisition)

        if (!shipment) {
            throw new IllegalArgumentException("Could not find shipment for stock movement with ID ${stockMovement.id}")
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
        Container box = (boxName) ? pallet.findOrCreateBox(boxName) : null
        return box ?: pallet ?: null
    }

    void createMissingShipmentItems(StockMovement stockMovement) {
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
            }
        } else {
            requisition.requisitionItems?.each { RequisitionItem requisitionItem ->
                List<ShipmentItem> shipmentItems = createShipmentItems(requisitionItem)

                shipmentItems.each { ShipmentItem shipmentItem ->
                    shipment.addToShipmentItems(shipmentItem)
                }
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

    StockMovement updatePackPageItems(StockMovement stockMovement) {
        if (stockMovement?.packPage?.packPageItems) {
            stockMovement.packPage.packPageItems.each { PackPageItem packPageItem ->
                updateShipmentItemAndProcessSplitLines(packPageItem)
            }
        }

        stockMovement.packPage = getPackPage(stockMovement.id)

        return stockMovement
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

    void sendStockMovement(String id) {

        User user = AuthService.currentUser.get()
        StockMovement stockMovement = getStockMovement(id)
        Requisition requisition = stockMovement.requisition
        def shipment = requisition.shipment

        validateRequisition(requisition)

        if (!shipment) {
            throw new IllegalStateException("There are no shipments associated with stock movement ${requisition.requestNumber}")
        }

        shipmentService.sendShipment(shipment, null, user, requisition.origin, stockMovement.dateShipped ?: new Date())
        requisition.requisitionItems.each { requisitionItem ->
            if (requisitionItem.orderItem) {
                OrderItem orderItem = OrderItem.get(requisitionItem.orderItem.id)
                orderService.updateOrderItem(orderItem, requisitionItem.quantity)
            }
        }
    }

    void validateRequisition(Requisition requisition) {

        requisition.requisitionItems.each { requisitionItem ->
            if (!requisition.origin.isSupplier() && requisition.origin.supports(ActivityCode.MANAGE_INVENTORY)) {
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
        Shipment shipment = stockMovement?.requisition?.shipment
        if (shipment && shipment.currentStatus > ShipmentStatusCode.PENDING) {
            shipmentService.rollbackLastEvent(shipment)
            requisitionService.rollbackRequisition(requisition)
            requisition.requisitionItems.each { requisitionItem ->
                if (requisitionItem.orderItem) {
                    OrderItem orderItem = OrderItem.get(requisitionItem.orderItem.id)
                    orderService.updateOrderItem(orderItem, -requisitionItem.quantity)
                }
            }
        }
    }


    List<Map> getDocuments(StockMovement stockMovement) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
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
                            name        : g.message(code: "deliveryNote.label", default: "Delivery Note"),
                            documentType: DocumentGroupCode.DELIVERY_NOTE.name(),
                            contentType : "text/html",
                            stepNumber  : 5,
                            uri         : g.createLink(controller: 'deliveryNote', action: "print", id: stockMovement?.requisition?.id, absolute: true)
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
                    ]

            ])
        }

        if (stockMovement?.shipment) {
            ShipmentWorkflow shipmentWorkflow = shipmentService.getShipmentWorkflow(stockMovement?.shipment)
            log.info "Shipment workflow " + shipmentWorkflow
            if (shipmentWorkflow) {
                shipmentWorkflow.documentTemplates.each { Document documentTemplate ->
                    documentList << [
                            name        : documentTemplate?.name,
                            documentType: documentTemplate?.documentType?.name,
                            contentType : documentTemplate?.contentType,
                            stepNumber  : null,
                            uri         : g.createLink(controller: 'document', action: "render",
                                    id: documentTemplate?.id, params: [shipmentId: stockMovement?.shipment?.id],
                                    absolute: true, title: documentTemplate?.filename)
                    ]
                }
            }

            stockMovement?.shipment?.documents.each { Document document ->
                def action = document.documentType.documentCode == DocumentCode.SHIPPING_TEMPLATE ? "render" : "download"
                documentList << [
                        name        : document?.name,
                        documentType: document?.documentType?.name,
                        contentType : document?.contentType,
                        stepNumber  : null,
                        uri         : g.createLink(controller: 'document', action: action,
                                id: document?.id, params: [shipmentId: stockMovement?.shipment?.id],
                                absolute: true, title: document?.filename)
                ]
            }

        }

        return documentList
    }

    List buildStockMovementItemList(StockMovement stockMovement) {
        // We need to create at least one row to ensure an empty template
        if (stockMovement?.lineItems?.empty) {
            stockMovement?.lineItems.add(new StockMovementItem())
        }

        def lineItems = stockMovement.lineItems.collect {
            [
                "Requisition item id"            : it?.id ?: "",
                "Product code (required)"     : it?.product?.productCode ?: "",
                "Product name"                  : it?.product?.name ?: "",
                "Pack level 1"                   : it?.palletName ?: "",
                "Pack level 2"                      : it?.boxName ?: "",
                "Lot number"                    : it?.lotNumber ?: "",
                "Expiration date (MM/dd/yyyy)": it?.expirationDate ? it?.expirationDate?.format("MM/dd/yyyy") : "",
                "Quantity (required)"        : it?.quantityRequested ?: "",
                "Recipient id"                  : it?.recipient?.id ?: ""
            ]
        }
        return lineItems
    }
}

