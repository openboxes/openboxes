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

import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.DocumentGroupCode
import org.pih.warehouse.api.EditPage
import org.pih.warehouse.api.EditPageItem
import org.pih.warehouse.api.PickPage
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.SubstitutionItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

class StockMovementService {

    def productService
    def identifierService
    def requisitionService
    def shipmentService
    def inventoryService

    boolean transactional = true

    def grailsApplication

    def createStockMovement(StockMovement stockMovement) {

        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }

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

        requisition.name = stockMovement.name;
        requisition.description = stockMovement.description
        requisition.destination = stockMovement.destination
        requisition.origin = stockMovement.origin
        requisition.name = stockMovement.name
        requisition.requestedBy = stockMovement.requestedBy
        requisition.dateRequested = stockMovement.dateRequested

        // If the user specified a stocklist then we should automatically clone it as long as there are no
        // requisition items already added to the requisition
        if (stockMovement.stocklist && !requisition.requisitionItems) {
            stockMovement.stocklist.requisitionItems.each { stocklistItem ->
                RequisitionItem requisitionItem = new RequisitionItem()
                requisitionItem.product = stocklistItem.product
                requisitionItem.quantity = stocklistItem.quantity
                requisitionItem.orderIndex = stocklistItem.orderIndex
                requisition.addToRequisitionItems(requisitionItem)
            }
        }
        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        requisition = requisition.refresh()

        return StockMovement.createFromRequisition(requisition)
    }

    void updateStatus(String id, RequisitionStatus status) {
        Requisition requisition = Requisition.get(id)
        if (!status in RequisitionStatus.list()) {
            throw new IllegalStateException("Transition from ${requisition.status.name()} to ${status.name()} is not allowed")
        } else if (status < requisition.status) {
            throw new IllegalStateException("Transition from ${requisition.status.name()} to ${status.name()} is not allowed - use rollback instead")
        }

        requisition.status = status
        requisition.save(flush: true)
    }


    StockMovement updateStockMovement(StockMovement stockMovement) {
        log.info "Update stock movement " + stockMovement + " stockMovement.lineItems = " + stockMovement?.lineItems

        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }

        if (stockMovement.identifier) requisition.requestNumber = stockMovement.identifier
        if (stockMovement.destination) requisition.destination = stockMovement.destination
        if (stockMovement.origin) requisition.origin = stockMovement.origin
        if (stockMovement.name) requisition.name = stockMovement.name
        if (stockMovement.description) requisition.description = stockMovement.description
        if (stockMovement.requestedBy) requisition.requestedBy = stockMovement.requestedBy
        if (stockMovement.dateRequested) requisition.dateRequested = stockMovement.dateRequested

        if (stockMovement.dateShipped && stockMovement.shipmentType) {
            log.info "Creating shipment for stock movement ${stockMovement}"
            createOrUpdateShipment(stockMovement)
        }


        if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                RequisitionItem requisitionItem
                // Try to find a matching stock movement item
                if (stockMovementItem.id) {
                    requisitionItem = requisition.requisitionItems.find { it.id == stockMovementItem.id }
                    // We should not just assume that if
                    if (!requisitionItem) {
                        throw new IllegalArgumentException("Could not find stock movement item with ID ${stockMovementItem.id}")
                    }
                }

                // If requisition item is found, we update it
                if (requisitionItem) {
                    log.info "Item found " + requisitionItem.id

                    if (stockMovementItem.delete) {
                        log.info "Item deleted " + requisitionItem.id
                        requisitionItem.undoChanges()
                        requisition.removeFromRequisitionItems(requisitionItem)
                        requisitionItem.delete(flush: true)
                    } else if (stockMovementItem.revert) {
                        log.info "Item reverted " + requisitionItem.id
                        requisitionItem.undoChanges()
                    } else if (stockMovementItem.cancel) {
                        log.info "Item canceled " + requisitionItem.id
                        requisitionItem.cancelQuantity(stockMovementItem.reasonCode, stockMovementItem.comments)
                    } else if (stockMovementItem.substitute) {
                        log.info "Item substituted " + requisitionItem.id
                        log.info "Substitutions: " + requisitionItem.product.substitutions
                        if (!requisitionItem.product.isValidSubstitution(stockMovementItem?.newProduct)) {
                            throw new IllegalArgumentException("Product ${stockMovementItem?.newProduct?.productCode} " +
                                    "${stockMovementItem?.newProduct?.name} is not a valid substitution of " +
                                    "${requisitionItem?.product?.productCode} ${requisitionItem?.product?.name}")
                        }
                        requisitionItem.chooseSubstitute(
                                stockMovementItem.newProduct,
                                null,
                                stockMovementItem.newQuantity?.intValueExact(),
                                stockMovementItem.reasonCode,
                                stockMovementItem.comments)
                    } else {
                        log.info "Item updated " + requisitionItem.id
                        if (stockMovementItem.product) requisitionItem.product = stockMovementItem.product
                        if (stockMovementItem.inventoryItem) requisitionItem.inventoryItem = stockMovementItem.inventoryItem
                        if (stockMovementItem.quantityRequested) requisitionItem.quantity = stockMovementItem.quantityRequested
                        //if (stockMovementItem.recipient) requisitionItem.recipient = stockMovementItem.recipient
                        if (stockMovementItem.sortOrder) requisitionItem.orderIndex = stockMovementItem.sortOrder
                        if (stockMovementItem.quantityRevised) {
                            requisitionItem.changeQuantity(
                                    stockMovementItem?.quantityRevised?.intValueExact(),
                                    stockMovementItem.reasonCode,
                                    stockMovementItem.comments)
                        }
                    }
                    requisitionItem.save()
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
                    //requisitionItem.recipient = stockMovementItem.recipient
                    requisitionItem.orderIndex = stockMovementItem.sortOrder
                    requisition.addToRequisitionItems(requisitionItem)
                }
            }
        }

        if (requisition.hasErrors() || !requisition.save(flush: true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        requisition = requisition.refresh()

        return StockMovement.createFromRequisition(requisition)
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

    List<StockMovementItem> getStockMovements(Integer maxResults, Integer offset) {
        def requisitions = Requisition.findAllByIsTemplate(Boolean.FALSE, [max: maxResults, offset: offset, sort: "dateCreated", order: "desc"])
        def stockMovements = requisitions.collect { requisition ->
            return StockMovement.createFromRequisition(requisition)
        }
        return stockMovements
    }

    StockMovement getStockMovement(String id) {
        return getStockMovement(id, null)
    }

    StockMovement getStockMovement(String id, String stepNumber) {
        Requisition requisition = Requisition.get(id)
        if (!requisition) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }

        StockMovement stockMovement = StockMovement.createFromRequisition(requisition)
        stockMovement.documents = getDocuments(stockMovement)
        if (stepNumber.equals("3")) {
            stockMovement.lineItems = null
            stockMovement.editPage = getEditPage(id)
        } else if (stepNumber.equals("4")) {
            stockMovement.lineItems = null
            stockMovement.pickPage = getPickPage(id)
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
        Picklist picklist = requisitionItem?.requisition?.picklist
        log.info "Clear picklist"
        if (picklist) {
            picklist.picklistItems.findAll { it.requisitionItem == requisitionItem }.toArray().each {
                picklist.removeFromPicklistItems(it)
            }
            picklist.save()
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
            createPicklist(stockMovementItem)
        }
    }

    /**
     * Create an automated picklist for the given stock movement item.
     *
     * @param id
     */
    void createPicklist(StockMovementItem stockMovementItem) {

        log.info "Create picklist for stock movement item ${stockMovementItem}"

        // This is kind of a hack, but it's the only way I could figure out how to get the origin field
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        Product product = requisitionItem.product
        Location location = requisitionItem?.requisition?.origin
        Integer quantityRequested = requisitionItem.quantity

        // Retrieve all available items and then calculate suggested
        List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, product)
        log.info "Available items: ${availableItems}"
        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequested)
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
        // Update picklist item
        else {
            picklistItem.requisitionItem = requisitionItem
            picklistItem.inventoryItem = inventoryItem
            picklistItem.binLocation = binLocation
            picklistItem.quantity = quantity
            picklistItem.reasonCode = reasonCode
            picklistItem.comment = comment
        }
        picklist.save(flush: true)
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
        return availableSubstitutions
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
                return buildPickPageItem(it)
            }
        } else {
            pickPageItems << buildPickPageItem(requisitionItem)
        }
        return pickPageItems
    }


    EditPageItem buildEditPageItem(StockMovementItem stockMovementItem) {
        EditPageItem editPageItem = new EditPageItem()
        RequisitionItem requisitionItem = RequisitionItem.load(stockMovementItem.id)
        Location location = requisitionItem?.requisition?.origin
        List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, requisitionItem.product)
        List<SubstitutionItem> availableSubstitutions = getAvailableSubstitutions(location, requisitionItem.product)
        editPageItem.requisitionItem = requisitionItem
        editPageItem.productId = requisitionItem.product.id
        editPageItem.productCode = requisitionItem.product.productCode
        editPageItem.productName = requisitionItem.product.name
        editPageItem.quantityRequested = requisitionItem.quantity
        editPageItem.quantityConsumed = 0
        editPageItem.availableSubstitutions = availableSubstitutions
        editPageItem.availableItems = availableItems
        return editPageItem
    }

    /**
     *
     * @param requisitionItem
     * @return
     */
    PickPageItem buildPickPageItem(RequisitionItem requisitionItem) {

        PickPageItem pickPageItem = new PickPageItem(requisitionItem: requisitionItem,
                picklistItems: requisitionItem.picklistItems)
        Location location = requisitionItem?.requisition?.origin
        List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, requisitionItem.product)
        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, requisitionItem.quantity)
        pickPageItem.availableItems = availableItems
        pickPageItem.suggestedItems = suggestedItems

        return pickPageItem

    }

    Shipment createOrUpdateShipment(StockMovement stockMovement) {
        Shipment shipment = Shipment.findByRequisition(stockMovement.requisition)
        if (!shipment) shipment = new Shipment()
        shipment.name = stockMovement.generateName()
        shipment.origin = stockMovement.origin
        shipment.destination = stockMovement.destination
        shipment.requisition = stockMovement.requisition
        shipment.expectedShippingDate = stockMovement.dateShipped
        shipment.shipmentType = stockMovement.shipmentType

        // FIXME Associated tracking number and driver name with reference number and carrier (respectively)
        String additionalInformation = ""
        if (stockMovement.comments) additionalInformation += "Comments: ${stockMovement.comments}\n"
        if (stockMovement.trackingNumber) additionalInformation += "Tracking Number: ${stockMovement.trackingNumber}\n"
        if (stockMovement.driverName) additionalInformation += "Driver Name: ${stockMovement.driverName}\n"
        shipment.additionalInformation = additionalInformation

        stockMovement.requisition.picklist.picklistItems.collect { PicklistItem picklistItem ->
            ShipmentItem shipmentItem = createOrUpdateShipmentItem(picklistItem)
            shipment.addToShipmentItems(shipmentItem)
        }
        return shipment.save()
    }

    ShipmentItem createOrUpdateShipmentItem(PicklistItem picklistItem) {
        ShipmentItem shipmentItem = ShipmentItem.findByRequisitionItem(picklistItem?.requisitionItem)
        if (!shipmentItem) shipmentItem = new ShipmentItem()
        shipmentItem.lotNumber = picklistItem?.inventoryItem?.lotNumber
        shipmentItem.expirationDate = picklistItem?.inventoryItem?.expirationDate
        shipmentItem.product = picklistItem?.inventoryItem?.product
        shipmentItem.quantity = picklistItem?.quantity
        shipmentItem.requisitionItem = picklistItem.requisitionItem
        shipmentItem.recipient = picklistItem?.requisitionItem?.recipient
        shipmentItem.inventoryItem = picklistItem?.inventoryItem
        //shipmentItem.container = picklistItem?.requisitionItem?.palletName
        shipmentItem.binLocation = picklistItem?.binLocation
        return shipmentItem
    }


    void sendStockMovement(String id) {

        User user = AuthService.currentUser.get()
        StockMovement stockMovement = getStockMovement(id)
        Requisition requisition = stockMovement.requisition
        def shipments = requisition.shipments

        if (!shipments) {
            throw new IllegalStateException("There are no shipments associated with stock movement ${requisition.requestNumber}")
        }

        if (shipments.size() > 1) {
            throw new IllegalStateException("There are too many shipments associated with stock movement ${requisition.requestNumber}")
        }

        shipmentService.sendShipment(shipments[0], null, user, requisition.origin, new Date())
    }

    void rollbackStockMovement(String id) {
        StockMovement stockMovement = getStockMovement(id)
        Requisition requisition = stockMovement?.requisition
        if (requisition) requisitionService.rollbackRequisition(requisition)

        // If the shipment has been shipped we can roll it back
        Shipment shipment = stockMovement?.requisition?.shipments[0]
        if (shipment) {
            if (shipment.currentStatus == ShipmentStatusCode.SHIPPED) {
                shipmentService.rollbackLastEvent(shipment)
            }
            // If shipment status is any other status except pending then we should throw an error since rolling it
            // back would cause issues
            else if (shipment.currentStatus != ShipmentStatusCode.PENDING) {
                throw new IllegalStateException("Cannot rollback status for shipment ${shipment.shipmentNumber} from ${shipment.currentStatus}")
            }

        }
    }


    List<Map> getDocuments(StockMovement stockMovement) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        def documentList = [
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
                        uri         : g.createLink(controller: 'picklist', action: "renderPdf", id: stockMovement?.requisition?.id, absolute: true)
                ],
                [
                        name        : g.message(code: "shipping.printPickList.label"),
                        documentType: DocumentGroupCode.PICKLIST.name(),
                        contentType : "text/html",
                        stepNumber  : 5,
                        uri         : g.createLink(controller: 'report', action: "printPickListReport", params: ["shipment.id": stockMovement?.shipment?.id], absolute: true)
                ],
                [
                        name        : g.message(code: "shipping.printShippingReport.label"),
                        documentType: DocumentGroupCode.PACKING_LIST.name(),
                        contentType : "text/html",
                        stepNumber  : 5,
                        uri         : g.createLink(controller: 'report', action: "printShippingReport", params: ["shipment.id": stockMovement?.shipment?.id], absolute: true)
                ],
                [
                        name        : g.message(code: "shipping.printPaginatedPackingListReport.label"),
                        documentType: DocumentGroupCode.PACKING_LIST.name(),
                        contentType : "text/html",
                        stepNumber  : 5,
                        uri         : g.createLink(controller: 'report', action: "printPaginatedPackingListReport", params: ["shipment.id": stockMovement?.shipment?.id], absolute: true)
                ],
                [
                        name        : g.message(code: "shipping.downloadPackingList.label"),
                        documentType: DocumentGroupCode.PACKING_LIST.name(),
                        contentType : "application/vnd.ms-excel",
                        stepNumber  : 5,
                        uri         : g.createLink(controller: 'doc4j', action: "downloadPackingList", id: stockMovement?.shipment?.id, absolute: true)
                ],
                [
                        name        : g.message(code: "shipping.downloadLetter.label"),
                        documentType: DocumentGroupCode.CERTIFICATE_OF_DONATION.name(),
                        contentType : "text/html",
                        stepNumber  : 5,
                        uri         : g.createLink(controller: 'doc4j', action: "downloadLetter", id: stockMovement?.shipment?.id, absolute: true)
                ],
                [
                        name        : g.message(code: "deliveryNote.button.print.label"),
                        documentType: DocumentGroupCode.DELIVERY_NOTE.name(),
                        contentType : "text/html",
                        stepNumber  : 5,
                        uri         : g.createLink(controller: 'deliveryNote', action: "print", id: stockMovement?.requisition?.id, absolute: true)
                ]
        ]
        return documentList
    }
}

