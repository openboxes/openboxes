/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.receiving

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.PartialReceiptContainer
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.RefreshProductAvailabilityEvent
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent

@Transactional
class ReceiptService {

    AuthService authService
    ShipmentService shipmentService
    InventoryService inventoryService
    LocationService locationService
    ReceiptIdentifierService receiptIdentifierService
    TransactionIdentifierService transactionIdentifierService
    GrailsApplication grailsApplication
    ProductAvailabilityService productAvailabilityService

    @Transactional(readOnly=true)
    PartialReceipt getPartialReceipt(String id, String stepNumber) {
        Shipment shipment = Shipment.get(id)
        if (!shipment) {
            throw new IllegalArgumentException("Unable to find shipment with ID ${id}")
        }

        // Getting pending receipts and try to create a partial receipt from them
        Set<Receipt> receipts = shipment.receipts.findAll { Receipt receipt -> receipt.receiptStatusCode == ReceiptStatusCode.PENDING }
        if (receipts?.size() > 1) {
            throw new IllegalStateException("Shipments should only have one pending receipt at any given time")
        }

        PartialReceipt partialReceipt
        Receipt receipt = receipts ? receipts.first() : null
        if (receipt) {
            boolean includeShipmentItems = stepNumber == "1"
            partialReceipt = getPartialReceiptFromReceipt(receipt, includeShipmentItems)
        } else {
            partialReceipt = getPartialReceiptFromShipment(shipment)
        }
        return partialReceipt
    }

    /**
     * Create a partial receipt based off the items left to receive from a shipment.
     *
     * @param shipment
     * @return
     */
    PartialReceipt getPartialReceiptFromShipment(Shipment shipment) {
        def currentUser = authService.currentUser
        PartialReceipt partialReceipt = new PartialReceipt()
        partialReceipt.shipment = shipment
        partialReceipt.recipient = currentUser
        partialReceipt.dateShipped = shipment.actualShippingDate
        partialReceipt.dateDelivered = shipment.actualDeliveryDate ?: new Date()

        String[] receivingLocationNames = [locationService.getReceivingLocationName(shipment?.shipmentNumber), "Receiving ${shipment?.shipmentNumber}"]
        Location defaultBinLocation = !shipment.destination.hasBinLocationSupport() ? null :
                locationService.findInternalLocation(shipment.destination, receivingLocationNames)

        def shipmentItemsByContainer = shipment.sortShipmentItemsBySortOrder().groupBy { it.container }
        shipmentItemsByContainer.collect { container, shipmentItems ->

            PartialReceiptContainer partialReceiptContainer = new PartialReceiptContainer(container: container)
            partialReceipt.partialReceiptContainers.add(partialReceiptContainer)

            shipmentItems.each { ShipmentItem shipmentItem ->
                partialReceiptContainer.partialReceiptItems.add(buildPartialReceiptItem(shipmentItem, defaultBinLocation))
            }
        }
        return partialReceipt
    }

    /**
     * Create partial receipt based on an existing pending receipt.
     *
     * @param receipt
     * @return
     */
    PartialReceipt getPartialReceiptFromReceipt(Receipt receipt, boolean includeShipmentItems) {

        PartialReceipt partialReceipt = new PartialReceipt()
        partialReceipt.receipt = receipt
        partialReceipt.shipment = receipt.shipment
        partialReceipt.recipient = receipt.recipient
        partialReceipt.dateShipped = receipt?.shipment?.actualShippingDate
        partialReceipt.dateDelivered = receipt.actualDeliveryDate

        String[] receivingLocationNames = [locationService.getReceivingLocationName(receipt.shipment?.shipmentNumber), "Receiving ${receipt.shipment?.shipmentNumber}"]
        Location defaultBinLocation = !receipt.shipment.destination.hasBinLocationSupport() ? null :
                locationService.findInternalLocation(receipt.shipment.destination, receivingLocationNames)

        def shipmentItemsByContainer = receipt.shipment.sortShipmentItemsBySortOrder().groupBy { it.container }
        shipmentItemsByContainer.collect { container, shipmentItems ->

            PartialReceiptContainer partialReceiptContainer = new PartialReceiptContainer(container: container)

            shipmentItems.each { ShipmentItem shipmentItem ->
                Set<ReceiptItem> pendingReceiptItems =
                        receipt.receiptItems.findAll { ReceiptItem receiptItem -> receiptItem.shipmentItem == shipmentItem }

                if (pendingReceiptItems) {
                    pendingReceiptItems.each { ReceiptItem receiptItem ->
                        partialReceiptContainer.partialReceiptItems.add(buildPartialReceiptItem(receiptItem))
                    }
                } else if (includeShipmentItems) {
                    partialReceiptContainer.partialReceiptItems.add(buildPartialReceiptItem(shipmentItem, defaultBinLocation))
                }
            }

            if (partialReceiptContainer.partialReceiptItems) {
                partialReceipt.partialReceiptContainers.add(partialReceiptContainer)
            }
        }
        return partialReceipt
    }

    PartialReceiptItem buildPartialReceiptItem(ShipmentItem shipmentItem, Location binLocation) {
        PartialReceiptItem partialReceiptItem = new PartialReceiptItem()
        partialReceiptItem.shipmentItem = shipmentItem
        partialReceiptItem.recipient = shipmentItem.recipient
        if (binLocation) {
            partialReceiptItem.binLocation = binLocation
        }
        partialReceiptItem.lotNumber = shipmentItem.inventoryItem?.lotNumber
        partialReceiptItem.expirationDate = shipmentItem.inventoryItem?.expirationDate
        partialReceiptItem.quantityShipped = shipmentItem?.quantity ?: 0
        partialReceiptItem.quantityOnHand = productAvailabilityService.getQuantityOnHand(shipmentItem.inventoryItem)

        return partialReceiptItem
    }

    PartialReceiptItem buildPartialReceiptItem(ReceiptItem receiptItem) {
        PartialReceiptItem partialReceiptItem = new PartialReceiptItem()
        partialReceiptItem.shipmentItem = receiptItem.shipmentItem
        partialReceiptItem.receiptItem = receiptItem
        partialReceiptItem.recipient = receiptItem.recipient
        partialReceiptItem.binLocation = receiptItem.binLocation
        partialReceiptItem.quantityReceiving = receiptItem.quantityReceived

        partialReceiptItem.lotNumber = receiptItem.inventoryItem?.lotNumber
        partialReceiptItem.expirationDate = receiptItem.inventoryItem?.expirationDate
        partialReceiptItem.quantityShipped = receiptItem?.quantityShipped ?: 0
        partialReceiptItem.isSplitItem = receiptItem.isSplitItem
        partialReceiptItem.comment = receiptItem.comment
        partialReceiptItem.quantityOnHand = productAvailabilityService.getQuantityOnHand(receiptItem.inventoryItem)

        return partialReceiptItem
    }

    ReceiptItem createOrUpdateReceiptItem(PartialReceiptItem partialReceiptItem) {
        if (!partialReceiptItem.shipmentItem) {
            throw new IllegalArgumentException("Cannot receive item without valid shipment item")
        }

        InventoryItem inventoryItem =
                inventoryService.findOrCreateInventoryItem(partialReceiptItem.product, partialReceiptItem.lotNumber, partialReceiptItem.expirationDate)

        if (!inventoryItem) {
            throw new IllegalArgumentException("Cannot receive item without valid inventory item")
        }

        ReceiptItem receiptItem

        if (partialReceiptItem.receiptItem) {
            receiptItem = partialReceiptItem.receiptItem
        } else {
            receiptItem = new ReceiptItem()
            receiptItem.sortOrder = partialReceiptItem.shipmentItem.receiptItems.size()
        }

        receiptItem.binLocation = partialReceiptItem.binLocation
        receiptItem.recipient = partialReceiptItem.recipient
        receiptItem.quantityShipped = partialReceiptItem.quantityShipped
        receiptItem.quantityReceived = partialReceiptItem.quantityReceiving
        receiptItem.lotNumber = partialReceiptItem.lotNumber
        receiptItem.expirationDate = partialReceiptItem.expirationDate
        receiptItem.product = inventoryItem.product
        receiptItem.inventoryItem = inventoryItem
        receiptItem.shipmentItem = partialReceiptItem.shipmentItem
        receiptItem.isSplitItem = partialReceiptItem.isSplitItem
        receiptItem.comment = partialReceiptItem.comment

        if (partialReceiptItem.expirationDate && !partialReceiptItem.expirationDate.equals(inventoryItem.expirationDate)) {
            inventoryItem.expirationDate = partialReceiptItem.expirationDate
            inventoryItem.save(flush: true)
        }

        if (partialReceiptItem.cancelRemaining && ReceiptStatusCode.RECEIVED == receiptItem.receipt?.receiptStatusCode) {
            //when completing the pending receipt status was already changed to received and the item quantity will in quantityReceived,
            // so there is no need to subtract quantityReceiving, unless it's split item (which will always have quantity received = 0)
            Integer qtyCanceled = partialReceiptItem.quantityShipped - (partialReceiptItem.quantityReceived +
                    (partialReceiptItem.isSplitItem ? partialReceiptItem.quantityReceiving : 0))
            receiptItem.quantityCanceled = qtyCanceled
        }

        partialReceiptItem.receiptItem = receiptItem
        return receiptItem
    }

    void savePartialReceipt(PartialReceipt partialReceipt, boolean completed) {

        Shipment shipment = partialReceipt?.shipment
        Receipt receipt = partialReceipt?.receipt

        // Create new receipt
        if (!receipt) {
            receipt = new Receipt()
            receipt.receiptNumber = receiptIdentifierService.generate(receipt)
            shipment.addToReceipts(receipt)
        }

        receipt.receiptStatusCode = completed ? ReceiptStatusCode.RECEIVED : ReceiptStatusCode.PENDING

        receipt.recipient = partialReceipt.recipient
        receipt.shipment = partialReceipt.shipment
        receipt.expectedDeliveryDate = partialReceipt?.shipment?.expectedDeliveryDate
        receipt.actualDeliveryDate = partialReceipt.dateDelivered

        receipt.disableRefresh = true
        if (receipt.hasErrors() || !receipt.save()) {
            throw new ValidationException("Receipt is invalid", receipt.errors)
        }

        // Add receipt items
        partialReceipt.partialReceiptItems.each { partialReceiptItem ->

            if (partialReceiptItem.shouldSave) {
                ReceiptItem receiptItem = createOrUpdateReceiptItem(partialReceiptItem)
                receipt.addToReceiptItems(receiptItem)
                ShipmentItem shipmentItem = partialReceiptItem.shipmentItem
                shipmentItem.addToReceiptItems(receiptItem)
            }
        }

        // Save shipment
        shipment.save()
    }

    void saveAndCompletePartialReceipt(PartialReceipt partialReceipt) {
        Receipt.withTransaction { status ->
            try {
                // Create receipt, event, and transaction
                savePartialReceipt(partialReceipt, true)
                savePartialReceiptEvent(partialReceipt)
                Transaction transaction = createInboundTransaction(partialReceipt)

                // Trigger shipment status transition event to handle email notifications
                grailsApplication.mainContext.publishEvent(
                        new ShipmentStatusTransitionEvent(partialReceipt, ShipmentStatusCode.RECEIVED))

                // Trigger product availability refresh
                transaction.disableRefresh = Boolean.FALSE
                grailsApplication.mainContext.publishEvent(
                        new RefreshProductAvailabilityEvent(transaction, transaction.associatedLocation, transaction.associatedProducts, false))

            } catch (Exception e) {
                log.error "An unexpected error occurred during receipt: " + e.message, e
                throw e;
            }
        }
    }

    /**
     We can create RECEIVED or PARTIALLY_RECEIVED event depending on the case:
        1. When the location supports partial receiving
            RECEIVED - when the shipment is not already received and is fully received
            PARTIALLY_RECEIVED - when the shipment wasn't partially received before (it's
                created only once)
        2. When the location doesn't support partial receiving
            RECEIVED - after receiving for the first time, we cannot receive
                for the second time without partial receiving. The rest of the remaining quantities
                should be canceled
            PARTIALLY_RECEIVED - not allowed
     */
    void savePartialReceiptEvent(PartialReceipt partialReceipt) {
        Shipment shipment = partialReceipt.shipment
        Receipt receipt = partialReceipt.receipt

        if (!shipment.wasReceived() && (!shipment.destination.supports(ActivityCode.PARTIAL_RECEIVING) || shipment.isFullyReceived())) {
            shipmentService.createShipmentEvent(
                shipment,
                receipt?.actualDeliveryDate,
                EventCode.RECEIVED,
                shipment.destination,
            )
            return
        }

        if (!shipment.wasPartiallyReceived()) {
            shipmentService.createShipmentEvent(
                    shipment,
                    receipt?.actualDeliveryDate,
                    EventCode.PARTIALLY_RECEIVED,
                    shipment.destination,
            )
        }
    }

    Transaction createInboundTransaction(PartialReceipt partialReceipt) {

        Receipt receipt = partialReceipt.receipt
        if (!receipt) {
            throw new IllegalStateException("Must have a receipt")
        }

        Shipment shipment = partialReceipt.shipment
        if (!shipment) {
            throw new IllegalStateException("Must have a shipment")
        }

        if (!shipment?.destination?.inventory) {
            throw new IllegalStateException("Destination ${shipment?.destination?.name} must have an inventory in order to receive stock")
        }

        // Create a new transaction for incoming items
        Transaction creditTransaction = new Transaction()
        creditTransaction.transactionType = TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
        creditTransaction.incomingShipment = shipment
        creditTransaction.requisition = shipment?.requisition
        creditTransaction.receipt = receipt
        creditTransaction.source = shipment?.origin
        creditTransaction.destination = null
        creditTransaction.inventory = shipment?.destination?.inventory
        creditTransaction.transactionDate = receipt?.actualDeliveryDate
        creditTransaction.transactionNumber = transactionIdentifierService.generate(creditTransaction)

        receipt?.receiptItems?.each {
            def inventoryItem =
                    inventoryService.findOrCreateInventoryItem(it.product, it.lotNumber, it.expirationDate)

            if (inventoryItem.hasErrors()) {
                inventoryItem.errors.allErrors.each { error ->
                    def errorObj = [inventoryItem, error.field, error.rejectedValue] as Object[]
                    shipment.errors.reject("inventoryItem.invalid",
                            errorObj, "[${error.field} ${error.rejectedValue}] - ${error.defaultMessage} ")
                }
                throw new ValidationException("Failed to receive shipment while saving inventory item ", shipment.errors)
            }

            // Create a new transaction entry
            TransactionEntry transactionEntry = new TransactionEntry()
            transactionEntry.quantity = it.quantityReceived
            transactionEntry.binLocation = it.binLocation
            transactionEntry.inventoryItem = inventoryItem
            creditTransaction.addToTransactionEntries(transactionEntry)
        }

        // FIXME Block the refresh of the product availability table (to be triggered at end of request)
        creditTransaction.disableRefresh = Boolean.TRUE

        if (creditTransaction.hasErrors() || !creditTransaction.save(flush:true)) {
            // did not save successfully, display errors message
            throw new ValidationException("Failed to receive shipment due to error while saving transaction", creditTransaction.errors)
        }

        // Associate the incoming transaction with the shipment
        shipment.addToIncomingTransactions(creditTransaction)
        shipment.disableRefresh = false
        shipment.save(flush: true)

        return creditTransaction
    }


    void rollbackInboundTransactions(Shipment shipment) {
        if (shipment.incomingTransactions) {
            shipment.incomingTransactions?.toArray()?.each {
                shipment.removeFromIncomingTransactions(it)
                it.delete()
            }
        }
    }

    void rollbackPartialReceipts(Shipment shipment) {
        log.info "Rollback partial receipts for shipment " + shipment
        if (!shipment) {
            throw new IllegalArgumentException("Cannot rollback without valid shipment")
        }

        rollbackInboundTransactions(shipment)

        if (shipment.receipts) {
            shipment.receipts.toArray().each { Receipt receipt ->
                shipment.removeFromReceipts(receipt)
                receipt.delete()
            }

        }

        deleteEvent(shipment, EventCode.RECEIVED)
        deleteEvent(shipment, EventCode.PARTIALLY_RECEIVED)
    }

    void rollbackLastReceipt(Shipment shipment) {
        List<Receipt> receivedReceipts = shipment.receipts.findAll { Receipt receipt -> receipt.receiptStatusCode == ReceiptStatusCode.RECEIVED }.sort {
            it.dateCreated
        }

        if (receivedReceipts) {
            Receipt lastReceipt = receivedReceipts.last()

            validateReceiptForRollback(lastReceipt)

            Transaction transaction = shipment.incomingTransactions.find {
                it.receipt?.id == lastReceipt?.id
            }
            if (transaction) {
                shipment.removeFromIncomingTransactions(transaction)
                transaction.delete()
            }
            shipment.removeFromReceipts(lastReceipt)
            lastReceipt.delete()

            deleteEvent(shipment, EventCode.RECEIVED)

            if (receivedReceipts.size() <= 1) {
                deleteEvent(shipment, EventCode.PARTIALLY_RECEIVED)
            }
        }
    }

    void deleteEvent(Shipment shipment, EventCode eventCode) {
        Event event = shipment.events.find { it.eventType?.eventCode == eventCode }
        if (event) {
            shipmentService.deleteEvent(shipment, event)
        }
    }

    void validateReceiptForRollback(Receipt receipt) {
        Location location = receipt.shipment?.destination

        receipt.receiptItems?.each { item ->
            Integer quantityAvailable = inventoryService.getQuantityFromBinLocation(location, item.binLocation, item.inventoryItem)

            if (item.quantityReceived > quantityAvailable) {
                throw new IllegalStateException("Insufficient qty of product ${item.product?.productCode} ${item.product?.name} lot: ${item.inventoryItem?.lotNumber ?: ""} in bin: ${item.binLocation?.name ?: ""}")
            }
        }
    }

    void createTemporaryReceivingBin(Shipment shipment) {
        // Create temporary receiving area for the Partial Receipt process
        if (Holders.grailsApplication.config.openboxes.receiving.createReceivingLocation.enabled && shipment?.destination?.hasBinLocationSupport()) {
            LocationType locationType = LocationType.findByName("Receiving")
            if (!locationType) {
                throw new IllegalArgumentException("Unable to find location type 'Receiving'")
            }

            locationService.findOrCreateInternalLocation(shipment.shipmentNumber,
                    shipment.shipmentNumber, locationType, shipment.destination)
        }
    }
}
