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
import org.apache.commons.lang.NotImplementedException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.PickPage
import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment

class StockMovementService {

    def productService
    def identifierService
    def inventoryService

    boolean transactional = true

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
        if (requisition.hasErrors() || !requisition.save(flush:true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        requisition = requisition.refresh()

        return StockMovement.createFromRequisition(requisition)
    }

    def updateStatus(String id, RequisitionStatus status) {
        Requisition requisition = Requisition.get(id)
        requisition.status = status
        requisition.save(flush:true)
    }


    def updateStockMovement(StockMovement stockMovement) {
        log.info "Update stock movement " + stockMovement + " stockMovement.lineItems = " + stockMovement?.lineItems

        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }

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
                        requisitionItem.delete(flush:true)
                    }
                    else if (stockMovementItem.revert) {
                        log.info "Item reverted " + requisitionItem.id
                        requisitionItem.undoChanges()
                    }
                    else if (stockMovementItem.cancel) {
                        log.info "Item canceled " + requisitionItem.id
                        requisitionItem.cancelQuantity(stockMovementItem.reasonCode, stockMovementItem.comments)
                    }
                    else if (stockMovementItem.substitute) {
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
                    }
                    else {
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

        if (requisition.hasErrors() || !requisition.save(flush:true)) {
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
        def requisitions = Requisition.listOrderByDateCreated([max: maxResults, offset: offset, sort: "desc"])
        def stockMovements = requisitions.collect { requisition ->
            return StockMovement.createFromRequisition(requisition)
        }
        return stockMovements
    }

    StockMovement getStockMovement(String id) {
        return getStockMovement(id, null)
    }

    StockMovement getStockMovement(String id, String stepNumber) {
        Requisition requisition = Requisition.read(id)
        if (!requisition) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }

        StockMovement stockMovement = StockMovement.createFromRequisition(requisition)

        if (stepNumber.equals("3")) {
            // Hack way to include suggested and available items needed for step 3
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                List availableItems =
                        inventoryService.getAvailableItems(stockMovement.origin, stockMovementItem.product)
                //stockMovementItem.availableItems = availableItems
            }
        }
        else if (stepNumber.equals("4")) {
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
            picklist.picklistItems.toArray().each {
                picklist.removeFromPicklistItems(it)
            }
            picklist.save()
        }
    }

    void createPicklist(String id) {
        StockMovement stockMovement = getStockMovement(id)
        createPicklist(stockMovement)
    }

    void createPicklist(StockMovement stockMovement) {
        for (StockMovementItem stockMovementItem : stockMovement.lineItems) {
            createPicklist(stockMovementItem)
        }
    }


    void createPicklist(StockMovementItem stockMovementItem) {

        // This is kind of a hack, but it's the only way I could figure out how to get the origin field
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)
        Product product = requisitionItem.product
        Location location = requisitionItem?.requisition?.origin
        Integer quantityRequested = requisitionItem.quantity

        // Retrieve all available items and then calculate suggested
        List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, product)
        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequested)
        if (suggestedItems) {
            clearPicklist(stockMovementItem)
            for (SuggestedItem suggestedItem : suggestedItems) {
                createOrUpdatePicklistItem(stockMovementItem,
                        suggestedItem.inventoryItem,
                        suggestedItem.binLocation,
                        suggestedItem.quantityPicked.intValueExact(),
                        null,
                        null)
            }
        }
    }


    void createOrUpdatePicklistItem(StockMovementItem stockMovementItem, InventoryItem inventoryItem, Location binLocation,
                         Integer quantity, String reasonCode, String comment) {
        RequisitionItem requisitionItem = RequisitionItem.get(stockMovementItem.id)

        // Validate quantity
        // Cannot validate because this code cause the following exception:
        // PropertyValueException: not-null property references a null or transient value: org.pih.warehouse.picklist.PicklistItem.picklist
//        Location location = binLocation.parentLocation
//        List binLocations = inventoryService.getQuantityByBinLocation(location, binLocation)
//        binLocations = binLocations.findAll { it.inventoryItem == inventoryItem}
//        Integer quantityAvailable = binLocations.sum { it.quantity }
//
//        log.info ("Validation quantity available ${quantityAvailable} vs quantity requested ${quantity}")
//        if (quantityAvailable < quantity) {
//            throw new IllegalArgumentException("Bin location ${binLocation} does not have enough quantity " +
//                    "available ${quantityAvailable} to fulfill requested quantity ${quantity}.")
//        }

        def picklist = requisitionItem.requisition.picklist
        if (!picklist) {
            picklist = new Picklist()
            picklist.requisition = requisitionItem.requisition
        }

        // Locate picklist item by inventory item and bin location (unique)
        PicklistItem picklistItem = picklist.picklistItems.find {
            it.inventoryItem == inventoryItem && it.binLocation == binLocation
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
        picklist.save(flush:true)
    }

    /**
     * Get a list of suggested items for the given stock movement item.
     *
     * @param stockMovementItem
     * @return
     */
    List getSuggestedItems(List<AvailableItem> availableItems, Integer quantityRequested) {

        List suggestedItems = []

        // If there are no available items then we cannot reasonably suggest any
        if (!availableItems) {
            return suggestedItems
        }

        // As long as quantity requested is less than the total available we can iterate through available items
        // and pick until quantity requested is 0. Otherwise, we don't suggest anything because the user must
        // choose anyway. This might be improved in the future.
        Integer quantityAvailable = availableItems?.sum { it.quantityAvailable }
        if (quantityRequested <= quantityAvailable) {

            for (AvailableItem availableItem : availableItems) {
                if (quantityRequested == 0)
                    break

                // The quantity to pick is either the quantity available (if less than requested) or
                // the quantity requested (if less than available).
                int quantityPicked = (quantityRequested >= availableItem.quantityAvailable) ?
                        availableItem.quantityAvailable : quantityRequested

                log.info "Quantity picked ${quantityPicked}"
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
            pickPageItems << requisitionItem.substitutionItems.collect {
                return buildPickPageItem(it)
            }
        }
        else {
            pickPageItems << buildPickPageItem(requisitionItem)
        }
    }

    /**
     *
     * @param requisitionItem
     * @return
     */
    PickPageItem buildPickPageItem(RequisitionItem requisitionItem) {

        PickPageItem pickPageItem = new PickPageItem(requisitionItem: requisitionItem, picklistItems: requisitionItem.picklistItems)
        Location location = requisitionItem?.requisition?.origin
        List<AvailableItem> availableItems = inventoryService.getAvailableBinLocations(location, requisitionItem.product)
        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, requisitionItem.quantity)
        pickPageItem.availableItems = availableItems
        pickPageItem.suggestedItems = suggestedItems

        return pickPageItem

    }

}
