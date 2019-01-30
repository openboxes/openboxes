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
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
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
    def inventoryService
    def locationService

    boolean transactional = true

    def grailsApplication

    def createStockMovement(StockMovement stockMovement) {

        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }
        Requisition requisition = createRequisition(stockMovement)
        return StockMovement.createFromRequisition(requisition)
    }

    void updateStatus(String id, RequisitionStatus status) {

        log.info "Update status ${id} " + status

        StockMovement stockMovement = getStockMovement(id)
        Requisition requisition = Requisition.get(id)
        if (!status in RequisitionStatus.list()) {
            throw new IllegalStateException("Transition from ${requisition.status.name()} to ${status.name()} is not allowed")
        } else if (status < requisition.status) {
            // Ignore backwards state transitions since it occurs normally when users go back and edit pages earlier in the workflow
            log.warn("Transition from ${requisition.status.name()} to ${status.name()} is not allowed - use rollback instead")
        }
        else {
            requisition.status = status
            requisition.save(flush: true)
        }
    }


    StockMovement updateStockMovement(StockMovement stockMovement, Boolean forceUpdate) {
        // TODO: This function is a very good candidate for future refactor. This should be better split in case of
        // updating stock movement basing on origin type

        log.info "Update stock movement " + new JSONObject(stockMovement.toJson()).toString(4)

        Requisition requisition = updateRequisition(stockMovement, forceUpdate)

        if (stockMovement.origin.isSupplier()) {

            // After creating stock movement from Requisition in this case (when origin.isSupplier()), those 5 values were not
            // populated. As a quick fix, data that came from request is preserved and reapplied to SM afterwards.

            def driverName = stockMovement.driverName
            def trackingNumber = stockMovement.trackingNumber
            def comments = stockMovement.comments
            def shipmentType = stockMovement.shipmentType
            def dateShipped = stockMovement.dateShipped

            stockMovement = StockMovement.createFromRequisition(requisition)

            if (driverName) stockMovement.driverName = driverName
            if (trackingNumber) stockMovement.trackingNumber = trackingNumber
            if (comments) stockMovement.comments = comments
            stockMovement.shipmentType = shipmentType
            stockMovement.dateShipped = dateShipped
        }

        log.info "Date shipped: " + stockMovement.dateShipped
        if (RequisitionStatus.CHECKING == requisition.status || RequisitionStatus.PICKED == requisition.status || RequisitionStatus.ISSUED == requisition.status) {
            log.info "Creating shipment for stock movement ${stockMovement}"
            createOrUpdateShipment(stockMovement)
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        requisition = requisition.refresh()

        stockMovement = StockMovement.createFromRequisition(requisition)

        createMissingPicklistItems(stockMovement)
        createMissingShipmentItems(stockMovement)

        return stockMovement
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
        return getStockMovements(null, maxResults, offset)
    }


    def getStockMovements(StockMovement stockMovement, Integer maxResults, Integer offset) {
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
            }
            else {
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

            //if (offset) firstResult(offset)
            //if (maxResults) maxResults(maxResults)
            order("dateCreated", "desc")
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

        StockMovement stockMovement = StockMovement.createFromRequisition(requisition)
        stockMovement.documents = getDocuments(stockMovement)
        if (stepNumber.equals("3")) {
            stockMovement.lineItems = null
            stockMovement.editPage = getEditPage(id)
        }
        else if (stepNumber.equals("4")) {
            stockMovement.lineItems = null
            stockMovement.pickPage = getPickPage(id)
        }
        else if (stepNumber.equals("5")) {
            stockMovement.lineItems = null
            stockMovement.packPage = getPackPage(id)
        }
        else if (stepNumber.equals("6")) {
            if (!stockMovement.origin.isSupplier()) {
                stockMovement.lineItems = null
                stockMovement.packPage = getPackPage(id)
            }
        }

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
        if (requisitionItem.modificationItem) {
            requisitionItem = requisitionItem.modificationItem
        }
        Picklist picklist = requisitionItem?.requisition?.picklist
        log.info "Clear picklist"
        if (picklist) {
            picklist.picklistItems.findAll { it.requisitionItem == requisitionItem }.toArray().each {
                picklist.removeFromPicklistItems(it)
            }
            picklist.save()
        }
    }

    void createMissingPicklistItems(StockMovement stockMovement) {
        if (stockMovement.requisition?.status >= RequisitionStatus.PICKING) {
            stockMovement?.lineItems?.each { StockMovementItem stockMovementItem ->
                if (stockMovementItem.statusCode == 'SUBSTITUTED') {
                    for (StockMovementItem subStockMovementItem : stockMovementItem.substitutionItems) {
                        createMissingPicklistItems(subStockMovementItem)
                    }
                } else if (stockMovementItem.statusCode == 'CHANGED') {
                    if (!stockMovementItem.requisitionItem?.modificationItem?.picklistItems) {
                        createMissingPicklistItems(stockMovementItem)
                    }
                }
                else {
                    createMissingPicklistItems(stockMovementItem)
                }
            }
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
            }
            else {
                createPicklist(stockMovementItem)
            }
        }
    }

    /**
     * Create an automated picklist for the given stock movement item.
     *
     * @param id
     */
    void createPicklist(StockMovementItem stockMovementItem) {

        log.info "Create picklist for stock movement item ${stockMovementItem.toJson()}"

        // This is kind of a hack, but it's the only way I could figure out how to get the origin field
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        Product product = requisitionItem.product
        Location location = requisitionItem?.requisition?.origin
        Integer quantityRequired = requisitionItem?.calculateQuantityRequired()

        log.info "QUANTITY REQUIRED: ${quantityRequired}"

        if (quantityRequired) {
            // Retrieve all available items and then calculate suggested
            List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, product)
            log.info "Available items: ${availableItems}"
            List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequired)
            log.info "Suggested items " + suggestedItems
            if (suggestedItems) {
                clearPicklist(stockMovementItem)
                for (SuggestedItem suggestedItem : suggestedItems) {
                    createOrUpdatePicklistItem(stockMovementItem,
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

        // Remove from picklist
        if (quantity <= 0) {
            picklist.removeFromPicklistItems(picklistItem)
        }
        // Populate picklist item
        else {

            // If we've modified the requisition item we need to associate picks with the modified item
            if (requisitionItem.modificationItem) {
                requisitionItem = requisitionItem.modificationItem
            }
            picklistItem.requisitionItem = requisitionItem
            picklistItem.inventoryItem = inventoryItem
            picklistItem.binLocation = binLocation
            picklistItem.quantity = quantity
            picklistItem.reasonCode = reasonCode
            picklistItem.comment = comment
            picklistItem.sortOrder = stockMovementItem.sortOrder
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
        Integer quantityAvailable = availableItems ? availableItems?.sum { it.quantityAvailable } : 0
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
                def availableItems = inventoryService.getAvailableBinLocations(location, associatedProduct)

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
            List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, item.product)

            SubstitutionItem substitutionItem = new SubstitutionItem()
            substitutionItem.productId = item?.product?.id
            substitutionItem.productName = item?.product?.name
            substitutionItem.productCode = item?.product?.productCode
            substitutionItem.quantitySelected = item?.quantity
            substitutionItem.availableItems = availableItems
            return substitutionItem
        }
    }

    // These two methods do very different things
//    List<SubstitutionItem> getSubstitutionItems(StockMovementItem stockMovementItem) {
//        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)
//        List substitutionItems = requisitionItem?.substitutionItems?.collect { substitutionItem ->
//            List availableItems = getAvailableItems()
//            return SubstitutionItem.createFromRequisitionItem(requisitionItem)
//        }
//        return substitutionItems
//    }


    EditPage getEditPage(String id) {
        EditPage editPage = new EditPage()
        StockMovement stockMovement = getStockMovement(id)
        stockMovement.lineItems.each { stockMovementItem ->
            stockMovementItem.stockMovement = stockMovement
            EditPageItem editPageItem = buildEditPageItem(stockMovementItem)
            editPage.editPageItems.addAll(editPageItem)
        }
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

    Float calculateMonthlyStockListQuantity(StockMovementItem stockMovementItem) {
        Integer monthlyStockListQuantity = 0
        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)
        StockMovement stockMovement = stockMovementItem.stockMovement
        List<Requisition> stocklists = requisitionService.getRequisitionTemplates(stockMovement.origin)
        if (stocklists) {
            stocklists.each { stocklist ->
                def stocklistItems = stocklist.requisitionItems.findAll { it?.product?.id == requisitionItem?.product?.id }
                if (stocklistItems) {
                    monthlyStockListQuantity += stocklistItems.sum { Math.ceil(((Double) it?.quantity) / it?.requisition?.replenishmentPeriod * 30) }
                }
            }
        }
        return monthlyStockListQuantity
    }

    EditPageItem buildEditPageItem(StockMovementItem stockMovementItem) {
        EditPageItem editPageItem = new EditPageItem()
        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)
        Location location = requisitionItem?.requisition?.origin
        List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, requisitionItem.product)
        List<SubstitutionItem> availableSubstitutions = getAvailableSubstitutions(location, requisitionItem.product)
        List<SubstitutionItem> substitutionItems = getSubstitutionItems(location, requisitionItem)


        // Calculate monthly stock
        Integer monthlyStockListQuantity = calculateMonthlyStockListQuantity(stockMovementItem)

        editPageItem.requisitionItem = requisitionItem
        editPageItem.productId = requisitionItem.product.id
        editPageItem.productCode = requisitionItem.product.productCode
        editPageItem.productName = requisitionItem.product.name
        editPageItem.quantityRequested = requisitionItem.quantity
        editPageItem.quantityConsumed = monthlyStockListQuantity
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

        PickPageItem pickPageItem = new PickPageItem(requisitionItem: requisitionItem,
                picklistItems: requisitionItem.picklistItems)
        Location location = requisitionItem?.requisition?.origin
        List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, requisitionItem.product)

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
            for (ShipmentItem shipmentItem : shipmentItems) {
                packPageItems << buildPackPageItem(shipmentItem)
            }
        }

        return packPageItems
    }

    PackPageItem buildPackPageItem(ShipmentItem shipmentItem) {
        String palletName = ""
        String boxName = ""
        if(shipmentItem?.container?.parentContainer) {
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

        addStockListItemsToRequisition(stockMovement, requisition)
        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }
        return requisition
    }

    void addStockListItemsToRequisition(StockMovement stockMovement, Requisition requisition) {
        // If the user specified a stocklist then we should automatically clone it as long as there are no
        // requisition items already added to the requisition
        if (stockMovement.stocklist && !requisition.requisitionItems) {
            stockMovement.stocklist.requisitionItems.each { stocklistItem ->
                RequisitionItem requisitionItem = new RequisitionItem()
                requisitionItem.product = stocklistItem.product
                requisitionItem.quantity = stocklistItem.quantity
                requisitionItem.quantityApproved = stocklistItem.quantity
                requisitionItem.orderIndex = stocklistItem.orderIndex
                requisition.addToRequisitionItems(requisitionItem)
            }
        }
    }

    Requisition updateRequisition(StockMovement stockMovement, Boolean forceUpdate) {
        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }

        if (stockMovement.identifier) requisition.requestNumber = stockMovement.identifier
        if (stockMovement.destination) requisition.destination = stockMovement.destination
        if (stockMovement.origin) requisition.origin = stockMovement.origin
        if (stockMovement.description) requisition.description = stockMovement.description
        if (stockMovement.requestedBy) requisition.requestedBy = stockMovement.requestedBy
        if (stockMovement.dateRequested) requisition.dateRequested = stockMovement.dateRequested
        requisition.name = RequisitionStatus.ISSUED == requisition.status ? stockMovement.name : stockMovement.generateName()

        if (forceUpdate) {
            removeRequisitionItems(requisition)
            addStockListItemsToRequisition(stockMovement, requisition)
            requisition.requisitionTemplate = stockMovement.stocklist
        }
        else if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                RequisitionItem requisitionItem
                // Try to find a matching stock movement item
                if (stockMovementItem.id) {
                    requisitionItem = requisition.requisitionItems.find { it.id == stockMovementItem.id }
                    if (!requisitionItem) {
                        throw new IllegalArgumentException("Could not find stock movement item with ID ${stockMovementItem.id}")
                    }
                }

                // If requisition item is found, we update it
                if (requisitionItem) {
                    log.info "Item found " + requisitionItem.id

                    removeShipmentItemsForModifiedRequisitionItem(requisitionItem)

                    if (stockMovementItem.delete) {
                        log.info "Item deleted " + requisitionItem.id
                        requisitionItem.undoChanges()
                        requisition.removeFromRequisitionItems(requisitionItem)
                        requisitionItem.delete(flush: true)
                    } else if (stockMovementItem.revert) {
                        log.info "Item reverted " + requisitionItem.id
                        requisitionItem.undoChanges()
                        requisitionItem.quantityApproved = requisitionItem.quantity
                    } else if (stockMovementItem.cancel) {
                        log.info "Item canceled " + requisitionItem.id
                        requisitionItem.cancelQuantity(stockMovementItem.reasonCode, stockMovementItem.comments)
                        requisitionItem.quantityApproved = 0
                    } else if (stockMovementItem.substitute) {
                        log.info "Item substituted " + requisitionItem.id
                        log.info "Substitutions: " + requisitionItem.product.substitutions

                        //this is for split line during substitution (if substituted item has available quantity it shows up in the substitutions list)
                        if (requisitionItem.product == stockMovementItem.newProduct) {
                            Integer changedQuantity = requisitionItem.quantity - stockMovementItem.newQuantity?.intValueExact()
                            requisitionItem.quantity = changedQuantity > 0 ? changedQuantity : 0

                            RequisitionItem newItem = new RequisitionItem()
                            newItem.product = stockMovementItem.newProduct
                            newItem.quantity = stockMovementItem.newQuantity?.intValueExact() > 0 ? stockMovementItem.newQuantity?.intValueExact() : 0
                            newItem.quantityApproved = stockMovementItem.newQuantity?.intValueExact() > 0 ? stockMovementItem.newQuantity?.intValueExact() : 0
                            newItem.orderIndex = stockMovementItem.sortOrder
                            newItem.recipient = requisitionItem.recipient
                            newItem.palletName = requisitionItem.palletName
                            newItem.boxName = requisitionItem.boxName
                            newItem.lotNumber = requisitionItem.lotNumber
                            newItem.expirationDate = requisitionItem.expirationDate
                            newItem.requisition = requisition
                            newItem.save()

                            //when line is split all not substituted quantity goes to the split item, when it's higher than quantity chosen for this item, split item is revised
                            //newQuantity - calculated on frontend, it's original item quantity minus sum of all substitution items quantities
                            //quantityRevised - quantity selected by the user for the split line item
                            if (stockMovementItem.quantityRevised != null && stockMovementItem.quantityRevised.intValueExact() < stockMovementItem.newQuantity?.intValueExact()) {
                                newItem.changeQuantity(
                                        stockMovementItem?.quantityRevised?.intValueExact(),
                                        stockMovementItem.reasonCode,
                                        stockMovementItem.comments)
                                newItem.quantityApproved = 0
                            }

                            requisition.addToRequisitionItems(newItem)
                        } else if (!requisitionItem.product.isValidSubstitution(stockMovementItem?.newProduct)) {
                            throw new IllegalArgumentException("Product ${stockMovementItem?.newProduct?.productCode} " +
                                    "${stockMovementItem?.newProduct?.name} is not a valid substitution of " +
                                    "${requisitionItem?.product?.productCode} ${requisitionItem?.product?.name}")
                        } else {
                            requisitionItem.chooseSubstitute(
                                    stockMovementItem.newProduct,
                                    null,
                                    stockMovementItem.newQuantity?.intValueExact(),
                                    stockMovementItem.reasonCode,
                                    stockMovementItem.comments)
                            requisitionItem.quantityApproved = 0
                        }
                    } else if (stockMovementItem.quantityRevised != null) {
                        log.info "Item revised " + requisitionItem.id

                        // Cannot cancel quantity if it has already been canceled
                        if (!requisitionItem.quantityCanceled) {
                            requisitionItem.changeQuantity(
                                    stockMovementItem?.quantityRevised?.intValueExact(),
                                    stockMovementItem.reasonCode,
                                    stockMovementItem.comments)
                            requisitionItem.quantityApproved = 0
                        }
                    } else {
                        log.info "Item updated " + requisitionItem.id

                        if (stockMovementItem.quantityRequested && stockMovementItem.quantityRequested != requisitionItem.quantity) {
                            requisitionItem.undoChanges()
                        }

                        if (stockMovementItem.product) requisitionItem.product = stockMovementItem.product
                        if (stockMovementItem.quantityRequested) {
                            requisitionItem.quantity = stockMovementItem.quantityRequested
                            requisitionItem.quantityApproved = stockMovementItem.quantityRequested
                        }
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
                    requisition.addToRequisitionItems(requisitionItem)
                }
            }
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }
        return requisition
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
        removeShipmentItemsForModifiedRequisitionItem(requisitionItem)
        requisitionItem.undoChanges()
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

    Shipment createOrUpdateShipment(StockMovement stockMovement) {

        log.info "create or update shipment " + (new JSONObject(stockMovement.toJson())).toString(4)

        Shipment shipment
        if (stockMovement?.requisition) {
            shipment = Shipment.findByRequisition(stockMovement?.requisition)
        }

        if (!shipment) {
            shipment = new Shipment()
        }
        shipment.description = stockMovement.description
        shipment.origin = stockMovement.origin
        shipment.destination = stockMovement.destination
        shipment.requisition = stockMovement.requisition
        shipment.shipmentNumber = stockMovement.identifier
        shipment.additionalInformation = stockMovement.comments
        shipment.driverName = stockMovement.driverName

        // These values need defaults since they are not set until step 6
        shipment.expectedShippingDate = stockMovement.dateShipped?:new Date()

        // Set default shipment type so we can save to the database without user input
        shipment.shipmentType = stockMovement.shipmentType?:ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID)

        // Last step will be to update the generated name
        shipment.name = stockMovement.requisition.status == RequisitionStatus.ISSUED ? stockMovement.name : stockMovement.generateName()

        if(stockMovement.requisition.status == RequisitionStatus.ISSUED) {
            return shipment
        }

        if (stockMovement.trackingNumber) {
            ReferenceNumberType trackingNumberType = ReferenceNumberType.findById(Constants.TRACKING_NUMBER_TYPE_ID)
            if (!trackingNumberType) {
                throw new IllegalStateException("Must configure reference number type for Tracking Number with ID '${Constants.TRACKING_NUMBER_TYPE_ID}'")
            }

            // Needed to use ID since reference numbers is lazy loaded and equality operation was not working
            ReferenceNumber referenceNumber = shipment.referenceNumbers.find { ReferenceNumber refNum ->
                trackingNumberType?.id?.equals(refNum.referenceNumberType?.id)
            }

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
            shipment.save(failOnError: true)
        }

        if (stockMovement.origin.isSupplier()) {
            stockMovement.lineItems.collect { StockMovementItem stockMovementItem ->
                log.info "Create or update item ${stockMovementItem.toJson()}"
                Container container = createOrUpdateContainer(shipment, stockMovementItem.palletName, stockMovementItem.boxName)
                ShipmentItem shipmentItem = createOrUpdateShipmentItem(stockMovementItem)
                shipmentItem.container = container
                shipment.addToShipmentItems(shipmentItem)
            }
        } else if (stockMovement.packPage?.packPageItems) {
            stockMovement.packPage.packPageItems.each { PackPageItem packPageItem ->
                updateShipmentItemAndProcessSplitLines(packPageItem)
            }
        } else {
            createMissingShipmentItems(stockMovement.requisition, shipment)
        }

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }

        return shipment
    }

    ShipmentItem createOrUpdateShipmentItem(StockMovementItem stockMovementItem) {

        // FIXME Determine whether this will ever return multiple (my guess is no)
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        ShipmentItem shipmentItem = ShipmentItem.findByRequisitionItem(requisitionItem)

        if(!shipmentItem) {
            shipmentItem = new ShipmentItem()
        }

        InventoryItem inventoryItem = inventoryService.findOrCreateInventoryItem(stockMovementItem.product,
                        stockMovementItem.lotNumber, stockMovementItem.expirationDate)

        shipmentItem.requisitionItem = requisitionItem
        shipmentItem.product = stockMovementItem.product
        shipmentItem.inventoryItem = inventoryItem
        shipmentItem.lotNumber = inventoryItem.lotNumber
        shipmentItem.expirationDate = inventoryItem.expirationDate
        shipmentItem.quantity = stockMovementItem.quantityRequested
        shipmentItem.recipient = stockMovementItem.recipient
        return shipmentItem
    }


    Container createOrUpdateContainer(Shipment shipment, String palletName, String boxName) {
        if (boxName && !palletName) {
            throw IllegalArgumentException("A box must be contained within a pallet")
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
        requisition.requisitionItems?.each { RequisitionItem requisitionItem ->
            List<ShipmentItem> shipmentItems = createShipmentItems(requisitionItem)

            shipmentItems.each { ShipmentItem shipmentItem ->
                shipment.addToShipmentItems(shipmentItem)
            }
        }
    }

    List<ShipmentItem> createShipmentItems(RequisitionItem requisitionItem) {
        List<ShipmentItem> shipmentItems = new ArrayList<ShipmentItem>()

        if (ShipmentItem.findAllByRequisitionItem(requisitionItem)) {
            return shipmentItems
        }

        requisitionItem?.picklistItems?.each { PicklistItem picklistItem ->
            ShipmentItem shipmentItem = new ShipmentItem()
            shipmentItem.lotNumber = picklistItem?.inventoryItem?.lotNumber
            shipmentItem.expirationDate = picklistItem?.inventoryItem?.expirationDate
            shipmentItem.product = picklistItem?.inventoryItem?.product
            shipmentItem.quantity = picklistItem?.quantity
            shipmentItem.requisitionItem = picklistItem.requisitionItem
            shipmentItem.recipient = picklistItem?.requisitionItem?.recipient?:
                picklistItem?.requisitionItem?.parentRequisitionItem?.recipient
            shipmentItem.inventoryItem = picklistItem?.inventoryItem
            shipmentItem.binLocation = picklistItem?.binLocation

            shipmentItems.add(shipmentItem)
        }

        return shipmentItems
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

                splitItem.quantity = splitLineItem?.quantityShipped
                splitItem.recipient = splitLineItem?.recipient
                splitItem.container = createOrUpdateContainer(shipmentItem.shipment, splitLineItem?.palletName, splitLineItem?.boxName)

                splitItem.shipment.addToShipmentItems(splitItem)
                splitItem.save(flush: true)
            }
        }
        else if (shipmentItem) {
            shipmentItem.quantity = packPageItem?.quantityShipped
            shipmentItem.recipient = packPageItem?.recipient
            shipmentItem.container = createOrUpdateContainer(shipmentItem.shipment, packPageItem?.palletName, packPageItem?.boxName)
        }
    }

    void sendStockMovement(String id) {

        User user = AuthService.currentUser.get()
        StockMovement stockMovement = getStockMovement(id)
        Requisition requisition = stockMovement.requisition
        def shipment = requisition.shipment

        if (!shipment) {
            throw new IllegalStateException("There are no shipments associated with stock movement ${requisition.requestNumber}")
        }

        shipmentService.sendShipment(shipment, null, user, requisition.origin, stockMovement.dateShipped ?: new Date())

        // Create temporary receiving area for the Partial Receipt process
        if (grailsApplication.config.openboxes.receiving.createReceivingLocation.enabled && stockMovement.destination.hasBinLocationSupport()) {
            LocationType locationType = LocationType.findByName("Receiving")
            if (!locationType) {
                throw new IllegalArgumentException("Unable to find location type 'Receiving'")
            }
            String receivingLocationName = locationService.getReceivingLocationName(stockMovement?.identifier)
            locationService.findOrCreateInternalLocation(receivingLocationName,
                    stockMovement.identifier, locationType, stockMovement.destination)
        }
    }



    void rollbackStockMovement(String id) {
        StockMovement stockMovement = getStockMovement(id)

        // If the shipment has been shipped we can roll it back
        Requisition requisition = stockMovement?.requisition
        Shipment shipment = stockMovement?.requisition?.shipment
        if (shipment && shipment.currentStatus > ShipmentStatusCode.PENDING) {
            shipmentService.rollbackLastEvent(shipment)
        }
        else if (requisition) {
            requisitionService.rollbackRequisition(requisition)
        }
    }


    List<Map> getDocuments(StockMovement stockMovement) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        def documentList = []

        if (stockMovement?.requisition) {
            documentList.addAll([
                    [
                            name        : g.message(code: "export.items.label", default: "Export items for shipment creation"),
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
                            uri         : g.createLink(controller: 'goodsReceiptNote', action: "print", id: stockMovement?.shipment?.id, absolute: true)
                    ]
            ])
        }
//                [
//                        name        : g.message(code: "shipping.printPickList.label"),
//                        documentType: DocumentGroupCode.PICKLIST.name(),
//                        contentType : "text/html",
//                        stepNumber  : 5,
//                        uri         : g.createLink(controller: 'report', action: "printPickListReport", params: ["shipment.id": stockMovement?.shipment?.id], absolute: true)
//                ],
//                [
//                        name        : g.message(code: "shipping.printShippingReport.label"),
//                        documentType: DocumentGroupCode.PACKING_LIST.name(),
//                        contentType : "text/html",
//                        stepNumber  : 5,
//                        uri         : g.createLink(controller: 'report', action: "printShippingReport", params: ["shipment.id": stockMovement?.shipment?.id], absolute: true)
//                ],
//                [
//                        name        : g.message(code: "shipping.printPaginatedPackingListReport.label"),
//                        documentType: DocumentGroupCode.PACKING_LIST.name(),
//                        contentType : "text/html",
//                        stepNumber  : 5,
//                        uri         : g.createLink(controller: 'report', action: "printPaginatedPackingListReport", params: ["shipment.id": stockMovement?.shipment?.id], absolute: true)
//                ],

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
                            name        : g.message(code: "shipping.downloadRwandaCOD.label"),
                            documentType: DocumentGroupCode.RWANDA_COD.name(),
                            contentType : "application/vnd.ms-excel",
                            stepNumber  : 5,
                            uri         : g.createLink(controller: 'doc4j', action: "downloadRwandaCOD", id: stockMovement?.shipment?.id, absolute: true)
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
                            uri         : g.createLink(controller: 'document', action: "download",
                                    id: documentTemplate?.id, params: [shipmentId: stockMovement?.shipment?.id],
                                    absolute: true, title: documentTemplate?.filename)
                    ]
                }
            }

            stockMovement?.shipment?.documents.each { Document document ->
                documentList << [
                        name        : document?.name,
                        documentType: document?.documentType?.name,
                        contentType : document?.contentType,
                        stepNumber  : null,
                        uri         : g.createLink(controller: 'document', action: "download",
                                id: document?.id, params: [shipmentId: stockMovement?.shipment?.id],
                                absolute: true, title: document?.filename)
                ]
            }

        }

        return documentList
    }
}

