/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.fulfillment

import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.outbound.FulfillmentRequest
import org.pih.warehouse.outbound.ImportPackingListCommand
import org.pih.warehouse.outbound.ImportPackingListItem
import org.pih.warehouse.outbound.ShippingRequest
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent
import util.StringUtil


@Transactional
class FulfillmentService {

    IdentifierService identifierService
    ProductAvailabilityService productAvailabilityService

    /**
     * Adds a fulfillment item to a fulfillment object and saves the parent.
     * @param fulfillment
     * @param fulfillmentItem
     */
    void addToFulfillmentItems(Fulfillment fulfillment, FulfillmentItem fulfillmentItem) {

        log.info("Request: " + fulfillment.request)

        fulfillment.addToFulfillmentItems(fulfillmentItem)
        fulfillment.save()
    }


    /**
     * Returns fulfillment command object with a
     * @param id
     * @param fulfilledById
     * @return
     */
    FulfillmentCommand getFulfillment(String id, String fulfilledById) {
        def command = new FulfillmentCommand()

        // Make sure that the request we are trying to fulfill actually exists
        def requestInstance = Requisition.get(id)
        if (!requestInstance)
            throw new Exception("Unable to proceed with fulfillment without a valid request ")

        // Populate the command object with the
        command.request = requestInstance

        log.info("Request: " + requestInstance)
        log.info("Fulfillment: " + requestInstance.fulfillment)

        def fulfillment
        // Use existing fulfillment object
        if (requestInstance?.fulfillment) {
            fulfillment = requestInstance?.fulfillment
        }
        // Create a new fulfillment object
        else {
            fulfillment = new Fulfillment()
            fulfillment.status = FulfillmentStatus.NOT_FULFILLED
            fulfillment.dateFulfilled = new Date()
            fulfillment.fulfilledBy = Person.get(fulfilledById)
            fulfillment.request = requestInstance

            // Need to set the other side of the relationship as well
            requestInstance.fulfillment = fulfillment
        }
        command.fulfillment = fulfillment

        return command
    }

    StockMovement createOutbound(ImportPackingListCommand command) {
        // First create a requisition
        Requisition requisition = createRequisition(command.fulfillmentDetails)
        // Having a requisition we can create a shipment
        Shipment shipment = createShipment(command.sendingOptions, requisition)
        // Create requisition items and group them with the corresponding packing list item, to be able to indicate when creating a picklist item, what bin location
        // corresponds to a particular requisition item
        Map<RequisitionItem, ImportPackingListItem> requisitionItemsGrouped = createRequisitionItems(command.packingList, requisition)
        // Having requisition items, we can build a picklist
        createPicklist(requisition, requisitionItemsGrouped)
        // Build shipment items looking at picklist items associated with requisition items
        createShipmentItems(shipment, requisition)
        // Create a shipped event, associate it with the shipment, and issue the requisition
        sendShipment(shipment)
        // In the end create a debit transaction with its entries
        createOutboundTransaction(shipment)

        return StockMovement.createFromRequisition(requisition)
    }

    String generateName(Location origin, Location destination, Date dateRequested, String trackingNumber, String description) {
        final String separator =
                Holders.getConfig().getProperty("openboxes.generateName.separator") ?: Constants.DEFAULT_NAME_SEPARATOR

        String originIdentifier = origin?.locationNumber ?: origin?.name
        String destinationIdentifier = destination?.locationNumber ?: destination?.name
        StringBuilder name = new StringBuilder("${originIdentifier}${separator}${destinationIdentifier}")
        if (dateRequested) {
            name.append("${separator}${dateRequested?.format(Constants.GENERATE_NAME_DATE_FORMAT)}")
        }
        if (trackingNumber) {
            name.append("${separator}${trackingNumber}")
        }
        if (description) {
            name.append("${separator}${description}")
        }
        return StringUtil.removeWhiteSpace(name.toString())
    }

    Requisition createRequisition(FulfillmentRequest fulfillmentRequest) {
        Requisition requisition = new Requisition(
                status: RequisitionStatus.CREATED,
                requestNumber: identifierService.generateRequisitionIdentifier(),
                type: RequisitionType.IMPORT,
                description: fulfillmentRequest.description,
                destination: fulfillmentRequest.destination,
                origin: fulfillmentRequest.origin,
                requestedBy: fulfillmentRequest.requestedBy,
                dateRequested: fulfillmentRequest.dateRequested,
                name: generateName(fulfillmentRequest.origin, fulfillmentRequest.destination, fulfillmentRequest.dateRequested, null, fulfillmentRequest.description),
        )
        if (!requisition.validate()) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }
        return requisition.save()
    }

    Shipment createShipment(ShippingRequest shippingRequest, Requisition requisition) {
        Shipment shipment = new Shipment(
                requisition: requisition,
                shipmentNumber: requisition.requestNumber,
                origin: requisition.origin,
                destination: requisition.destination,
                description: requisition.description,
                expectedShippingDate: shippingRequest.expectedShippingDate,
                expectedDeliveryDate: shippingRequest.expectedDeliveryDate,
                shipmentType: shippingRequest.shipmentType,
                name: generateName(requisition.origin, requisition.destination, requisition.dateRequested, shippingRequest.trackingNumber, requisition.description),
        )
        createTrackingNumber(shipment, shippingRequest.trackingNumber)
        if (!shipment.validate()) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }
        return shipment.save()
    }


    ReferenceNumber createTrackingNumber(Shipment shipment, String trackingNumber) {
        ReferenceNumberType trackingNumberType = ReferenceNumberType.findById(Constants.TRACKING_NUMBER_TYPE_ID)
        if (!trackingNumberType) {
            throw new IllegalStateException("Must configure reference number type for Tracking Number with ID '${Constants.TRACKING_NUMBER_TYPE_ID}'")
        }
        ReferenceNumber referenceNumber = shipment.referenceNumbers.find { ReferenceNumber refNum ->
            trackingNumberType?.id?.equals(refNum.referenceNumberType?.id)
        }
        if (trackingNumber) {
            if (!referenceNumber) {
                referenceNumber = new ReferenceNumber()
                referenceNumber.identifier = trackingNumber
                referenceNumber.referenceNumberType = trackingNumberType
                shipment.addToReferenceNumbers(referenceNumber)
            } else {
                referenceNumber.identifier = trackingNumber
            }
        }
        return referenceNumber
    }

    Map<RequisitionItem, ImportPackingListItem> createRequisitionItems(List<ImportPackingListItem> packItems, Requisition requisition) {
        Map<RequisitionItem, ImportPackingListItem> requisitionItemsGrouped = new HashMap<>()
        List<RequisitionItem> requisitionItems = packItems.collect { ImportPackingListItem packItem ->
            InventoryItem inventoryItem = packItem?.product?.getInventoryItem(packItem?.lotNumber)
            RequisitionItem requisitionItem = new RequisitionItem(
                    product: packItem?.product,
                    inventoryItem: inventoryItem,
                    quantity: packItem?.quantityPicked,
                    quantityApproved: packItem?.quantityPicked,
                    recipient: packItem?.recipient,
                    requisition: requisition,
                    palletName: packItem?.palletName,
                    boxName: packItem?.boxName,
                    lotNumber: packItem?.lotNumber,
                    expirationDate: inventoryItem?.expirationDate,
            )
            if (requisitionItem.validate()){
                requisitionItemsGrouped.put(requisitionItem, packItem)
            }
            return requisitionItem
        }
        requisitionItems.each { RequisitionItem requisitionItem ->
            if (!requisitionItem.validate()) {
                throw new ValidationException("Invalid requisition item", requisitionItem.errors)
            }
            requisition.addToRequisitionItems(requisitionItem)
            requisitionItem.save()
        }

        return requisitionItemsGrouped
    }

    void createShipmentItems(Shipment shipment, Requisition requisition) {
        requisition.requisitionItems?.each { RequisitionItem requisitionItem ->
            List<ShipmentItem> shipmentItems = createShipmentItems(requisitionItem, shipment)
            shipmentItems.each { ShipmentItem shipmentItem ->
                shipmentItem.save()
                shipment.addToShipmentItems(shipmentItem)
            }
        }
    }

    Container createOrUpdateContainer(Shipment shipment, String palletName, String boxName) {
        if (boxName && !palletName) {
            throw new IllegalArgumentException("Please enter Pack level 1 before Pack level 2. A box must be contained within a pallet")
        }
        Container pallet = palletName ? shipment.findOrCreatePallet(palletName) : null
        if (pallet) {
            pallet.save()
        }
        Container box = boxName ? pallet.findOrCreateBox(boxName) : null
        return box ?: pallet ?: null
    }


    List<ShipmentItem> createShipmentItems(RequisitionItem requisitionItem, Shipment shipment) {
        List<ShipmentItem> shipmentItems = []

        requisitionItem?.picklistItems?.each { PicklistItem picklistItem ->
            if (picklistItem.quantity > 0) {
                ShipmentItem shipmentItem = new ShipmentItem(
                        lotNumber: picklistItem?.inventoryItem?.lotNumber,
                        expirationDate: picklistItem?.inventoryItem?.expirationDate,
                        product: picklistItem?.inventoryItem?.product,
                        quantity: picklistItem?.quantity,
                        requisitionItem: picklistItem.requisitionItem,
                        recipient: picklistItem?.requisitionItem?.recipient ?:
                                picklistItem?.requisitionItem?.parentRequisitionItem?.recipient,
                        inventoryItem: picklistItem?.inventoryItem,
                        binLocation: picklistItem?.binLocation,
                        sortOrder: shipmentItems.size(),
                        shipment: shipment,
                        container: createOrUpdateContainer(shipment, picklistItem?.requisitionItem?.palletName, picklistItem?.requisitionItem?.boxName),
                )
                shipmentItems.add(shipmentItem)
            }
        }
        return shipmentItems
    }

    Picklist createPicklist(Requisition requisition, Map<RequisitionItem, ImportPackingListItem> requisitionItemsGrouped) {
        Picklist picklist = new Picklist(requisition: requisition)
        requisition.picklist = picklist
        picklist.save()

        requisition.requisitionItems.each { RequisitionItem requisitionItem ->
            // Find corresponding packing list item to indicate what bin location belongs to a particular requisition item
            ImportPackingListItem correspondingPackListItem = requisitionItemsGrouped[requisitionItem]
            // Confirm again, that the quantity picked is in stock (the line below covers a case when someone adds the same item in a few lines)
            Integer quantity = productAvailabilityService.getQuantityAvailableToPromiseForProductInBin(requisition.origin, correspondingPackListItem.binLocation, requisitionItem.inventoryItem)
            if (requisitionItem.quantity > quantity) {
                // This needs to be type of Object[] in order for rejectValue to work
                Object[] errorArgs = [requisitionItem.quantity, requisitionItem.product?.productCode]
                requisitionItem.errors.rejectValue("quantity", "requisitionItem.quantity.overpick", errorArgs, "The quantity you have picked ({0}) for product ({1}) is not available in stock")
                throw new ValidationException("Invalid requisition item", requisitionItem.errors)
            }
            PicklistItem picklistItem = new PicklistItem(
                    inventoryItem: requisitionItem.inventoryItem,
                    binLocation: correspondingPackListItem.binLocation,
                    quantity: requisitionItem.quantity,
                    disableRefresh: true,
                    picklist: picklist,
                    requisitionItem: requisitionItem
            )
            requisitionItem.addToPicklistItems(picklistItem)
            picklist.addToPicklistItems(picklistItem)
            // Flush is needed in order for the product availability refresh method, to know about the persistence of the picklist item,
            // because the PA method runs in a separate session
            picklistItem.save(flush: true)
            productAvailabilityService.refreshProductsAvailability(
                    requisitionItem?.requisition?.origin?.id,
                    [requisitionItem?.inventoryItem?.product?.id],
                    [correspondingPackListItem?.binLocation?.id],
                    false)
        }

        return picklist
    }

    void sendShipment(Shipment shipment) {
        shipment.requisition.status = RequisitionStatus.ISSUED
        EventType eventType = EventType.findByEventCode(EventCode.SHIPPED)

        Event shippedEvent = new Event(
                createdBy: AuthService.currentUser,
                eventType: eventType,
                eventDate: new Date(),
                eventLocation: AuthService.currentLocation,
        )
        if (!shippedEvent.validate()) {
            throw new ValidationException("Invalid event", shippedEvent.errors)
        }
        shipment.addToEvents(shippedEvent)
        shippedEvent.save()

        Holders.grailsApplication.mainContext.publishEvent(new ShipmentStatusTransitionEvent(shipment, ShipmentStatusCode.SHIPPED))
    }

    Transaction createOutboundTransaction(Shipment shipment) {
        TransactionType debitTransactionType = TransactionType.read(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
        Transaction debitTransaction = new Transaction(
                source: null,
                destination: shipment?.destination,
                inventory: shipment?.origin?.inventory,
                transactionDate: shipment.actualShippingDate,
                requisition: shipment.requisition,
                transactionNumber: identifierService.generateTransactionIdentifier(),
                outgoingShipment: shipment,
                transactionType: debitTransactionType,
        )
        if (!debitTransaction.validate()) {
            throw new ValidationException("Invalid transaction", debitTransaction.errors)
        }
        debitTransaction.save()

        shipment.shipmentItems.each { ShipmentItem shipmentItem ->
            TransactionEntry transactionEntry = new TransactionEntry(
                    quantity: shipmentItem.quantity,
                    inventoryItem: shipmentItem.inventoryItem,
                    binLocation: shipmentItem.binLocation,
                    transaction: debitTransaction
            )
            if (!transactionEntry.validate()) {
                throw new ValidationException("Invalid transaction entry", transactionEntry.errors)
            }
            debitTransaction.addToTransactionEntries(transactionEntry)
            transactionEntry.save()
        }
        shipment.addToOutgoingTransactions(debitTransaction)

        return debitTransaction
    }
}
