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

import org.pih.warehouse.api.PartialReceipt
import org.pih.warehouse.api.PartialReceiptContainer
import org.pih.warehouse.api.PartialReceiptItem
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class ReceiptService {

    boolean transactional = true

    def shipmentService
    def inventoryService
    def locationService

    PartialReceipt getPartialReceipt(String id) {
        Shipment shipment = Shipment.get(id)

        PartialReceipt partialReceipt = new PartialReceipt()
        partialReceipt.shipment = shipment
        partialReceipt.recipient = shipment.recipient
        partialReceipt.dateShipped = shipment.actualShippingDate
        partialReceipt.dateDelivered = shipment.actualDeliveryDate ?: new Date()

        Location defaultBinLocation =
                locationService.findInternalLocation(shipment.destination, "Receiving ${shipment.shipmentNumber}")

        def shipmentItemsByContainer = shipment.shipmentItems.groupBy { it.container }
        shipmentItemsByContainer.collect { container, shipmentItems ->

            PartialReceiptContainer partialReceiptContainer = new PartialReceiptContainer()
            partialReceiptContainer.container = container
            partialReceipt.partialReceiptContainers.add(partialReceiptContainer)

            shipmentItems.collect { ShipmentItem shipmentItem ->
                if (shipmentItem.receiptItems) {
                    shipmentItem.receiptItems.collect { ReceiptItem receiptItem ->
                        partialReceiptContainer.partialReceiptItems.add(buildPartialReceiptItem(receiptItem))
                    }
                } else {
                    partialReceiptContainer.partialReceiptItems.add(buildPartialReceiptItem(shipmentItem, defaultBinLocation))
                }
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
        partialReceiptItem.quantityShipped = shipmentItem?.quantity?:0

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
        partialReceiptItem.quantityShipped = receiptItem?.quantityShipped?:0

        return partialReceiptItem
    }

    ReceiptItem createReceiptItem(PartialReceiptItem partialReceiptItem) {
        ShipmentItem shipmentItem = partialReceiptItem.shipmentItem
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

        if (partialReceiptItem.cancelRemaining) {
            receiptItem.quantityCanceled = shipmentItem.quantityRemaining - partialReceiptItem.quantityReceiving
        }

        partialReceiptItem.receiptItem = receiptItem
        return receiptItem
    }

    void savePartialReceipt(PartialReceipt partialReceipt) {

        log.info "Saving partial receipt " + partialReceipt

        Shipment shipment = partialReceipt?.shipment
        Receipt receipt = shipment?.receipt

        if (!receipt)
            receipt = new Receipt()

        // Update receipt header
        receipt.recipient = partialReceipt.recipient
        receipt.shipment = partialReceipt.shipment
        receipt.expectedDeliveryDate = partialReceipt.dateDelivered
        receipt.actualDeliveryDate = partialReceipt.dateDelivered
        receipt.save(flush:true)

        // Add receipt items
        partialReceipt.partialReceiptItems.each { partialReceiptItem ->

            log.info "Saving partial receipt item " + partialReceiptItem
            if (partialReceiptItem.quantityReceiving != null) {
                ReceiptItem receiptItem = createReceiptItem(partialReceiptItem)
                receipt.addToReceiptItems(receiptItem)
                ShipmentItem shipmentItem = partialReceiptItem.shipmentItem
                shipmentItem.addToReceiptItems(receiptItem)
            }
        }

        // Save shipment
        shipment.receipt = receipt
        shipment.save(flush:true)

        if (shipment.isFullyReceived()) {
            if (!shipment.wasReceived()) {
                shipmentService.createShipmentEvent(shipment,
                        shipment.receipt.actualDeliveryDate,
                        EventCode.RECEIVED,
                        shipment.destination);
            }
        }
        else {

            // Create received shipment event
            if (!shipment.wasPartiallyReceived()) {
                shipmentService.createShipmentEvent(shipment,
                        shipment.receipt.actualDeliveryDate,
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

        if (shipment.receipt) {
            shipment.receipt?.delete()
            shipment.receipt = null
        }
    }

}
