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

import grails.validation.ValidationException
import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.PartialReceiptContainer
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class ReceiptService {

    boolean transactional = true

    def shipmentService
    def inventoryService
    def locationService
    def identifierService

    PartialReceipt getPartialReceipt(String id) {
        Shipment shipment = Shipment.get(id)
        if (!shipment) {
            throw new IllegalArgumentException("Unable to find shipment with ID ${id}")
        }

        // Getting pending receipts and try to create a partial receipt from them
        Set<Receipt> receipts = shipment.receipts.findAll { Receipt receipt -> receipt.receiptStatusCode == ReceiptStatusCode.PENDING }
        if (receipts?.size() > 1) {
            throw IllegalStateException("Shipments should only have one pending receipt at any given time")
        }

        PartialReceipt partialReceipt
        Receipt receipt = receipts ? receipts.first() : null
        if (receipt) {
            partialReceipt = getPartialReceiptFromReceipt(receipt)
        }
        else {
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

        PartialReceipt partialReceipt = new PartialReceipt()
        partialReceipt.shipment = shipment
        partialReceipt.recipient = shipment.recipient
        partialReceipt.dateShipped = shipment.actualShippingDate
        partialReceipt.dateDelivered = shipment.actualDeliveryDate

        Location defaultBinLocation =
                locationService.findInternalLocation(shipment.destination, "Receiving ${shipment.shipmentNumber}")

        def shipmentItemsByContainer = shipment.shipmentItems.groupBy { it.container }
        shipmentItemsByContainer.collect { container, shipmentItems ->

            PartialReceiptContainer partialReceiptContainer = new PartialReceiptContainer(container:container)
            partialReceipt.partialReceiptContainers.add(partialReceiptContainer)

            shipmentItems.collect { ShipmentItem shipmentItem ->
                PartialReceiptItem partialReceiptItem = new PartialReceiptItem()
                partialReceiptItem.shipmentItem = shipmentItem
                partialReceiptItem.recipient = shipmentItem.recipient
                if (defaultBinLocation) {
                    partialReceiptItem.binLocation = defaultBinLocation
                }
                partialReceiptContainer.partialReceiptItems.add(partialReceiptItem)
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
    PartialReceipt getPartialReceiptFromReceipt(Receipt receipt) {

        PartialReceipt partialReceipt = new PartialReceipt()
        partialReceipt.shipment = receipt.shipment
        partialReceipt.recipient = receipt.recipient
        partialReceipt.dateShipped = receipt?.shipment?.actualShippingDate
        partialReceipt.dateDelivered = receipt.actualDeliveryDate

        Location defaultBinLocation =
                locationService.findInternalLocation(receipt?.shipment?.destination, "Receiving ${receipt?.shipment?.shipmentNumber}")

        def shipmentItemsByContainer = receipt.shipment.shipmentItems.groupBy { it.container }
        shipmentItemsByContainer.collect { container, shipmentItems ->

            PartialReceiptContainer partialReceiptContainer = new PartialReceiptContainer(container:container)
            partialReceipt.partialReceiptContainers.add(partialReceiptContainer)

            shipmentItems.collect { ShipmentItem shipmentItem ->

                // FIXME When building these partial receipt items for an existing receipt we need to build the
                // partial receipt items ...
                // a) from both the shipment item and its receipt item(s)
                // b) from the existing receipt item (in the case of split lines)

                // For scenario (b) we'll be able to use the receiptItem.id as the identity

                // Calculate pending quantity received
                Set<ReceiptItem> pendingReceiptItems =
                        receipt.receiptItems.findAll { ReceiptItem receiptItem -> receiptItem.shipmentItem == shipmentItem }
                Integer quantityReceiving = pendingReceiptItems.sum { it.quantityReceived } ?: null

                PartialReceiptItem partialReceiptItem = new PartialReceiptItem()
                partialReceiptItem.quantityReceiving = quantityReceiving
                partialReceiptItem.shipmentItem = shipmentItem
                partialReceiptItem.recipient = shipmentItem.recipient
                if (defaultBinLocation) {
                    partialReceiptItem.binLocation = defaultBinLocation
                }
                partialReceiptContainer.partialReceiptItems.add(partialReceiptItem)
            }
        }
        return partialReceipt
    }


    void savePartialReceipt(PartialReceipt partialReceipt) {

        log.info "Saving partial receipt " + partialReceipt

        Shipment shipment = partialReceipt?.shipment

        // Create new receipt
        Receipt receipt = new Receipt()
        receipt.receiptNumber = identifierService.generateReceiptIdentifier()
        receipt.receiptStatusCode = ReceiptStatusCode.PENDING
        receipt.recipient = partialReceipt.recipient
        receipt.shipment = partialReceipt.shipment
        receipt.expectedDeliveryDate = partialReceipt?.shipment?.expectedDeliveryDate
        receipt.actualDeliveryDate = partialReceipt.dateDelivered
        receipt.save(flush:true)

        // Add receipt items
        partialReceipt.partialReceiptItems.each { partialReceiptItem ->

            log.info "Saving partial receipt item " + partialReceiptItem
            if (partialReceiptItem.quantityReceiving != null) {
                ShipmentItem shipmentItem = partialReceiptItem.shipmentItem
                if (!shipmentItem) {
                    throw new IllegalArgumentException("Cannot receive item without valid shipment item")
                }

                InventoryItem inventoryItem =
                        inventoryService.findOrCreateInventoryItem(shipmentItem.product, shipmentItem.lotNumber, shipmentItem.expirationDate)

                if (!inventoryItem) {
                    throw new IllegalArgumentException("Cannot receive item without valid inventory item")
                }

                ReceiptItem receiptItem = new ReceiptItem();
                receiptItem.binLocation = partialReceiptItem.binLocation
                receiptItem.recipient = partialReceiptItem.recipient
                receiptItem.quantityShipped = shipmentItem.quantity;
                receiptItem.quantityReceived = partialReceiptItem.quantityReceiving
                receiptItem.lotNumber = shipmentItem.lotNumber;
                receiptItem.product = inventoryItem.product
                receiptItem.inventoryItem = inventoryItem
                receiptItem.shipmentItem = shipmentItem

                if (partialReceiptItem.cancelRemaining) {
                    receiptItem.quantityCanceled = shipmentItem.quantityRemaining - partialReceiptItem.quantityReceiving
                }

                receipt.addToReceiptItems(receiptItem)
                shipmentItem.addToReceiptItems(receiptItem)
            }
        }

        // Save shipment
        shipment.save(flush:true)

        if (shipment.isFullyReceived()) {
            if (!shipment.wasReceived()) {
                shipmentService.createShipmentEvent(shipment,
                        receipt.actualDeliveryDate,
                        EventCode.RECEIVED,
                        shipment.destination);
            }
        }
        else {

            // Create received shipment event
            if (!shipment.wasPartiallyReceived()) {
                shipmentService.createShipmentEvent(shipment,
                        receipt.actualDeliveryDate,
                        EventCode.PARTIALLY_RECEIVED,
                        shipment.destination);
            }
        }
    }

    void saveInboundTransaction(PartialReceipt partialReceipt) {
        Shipment shipment = partialReceipt.shipment
        if (shipment) {
            rollbackInboundTransactions(shipment)
            shipmentService.createInboundTransaction(shipment)
        }
    }

    void rollbackInboundTransactions(Shipment shipment) {
        if (shipment.incomingTransactions) {
            shipment.incomingTransactions?.toArray().each {
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
    }

}
