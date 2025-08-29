package org.pih.warehouse.inboundSortation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.shipping.Shipment

@Transactional
class InboundSortationService {

    def orderIdentifierService
    def putawayService

    void execute(Receipt receipt) {
        Shipment shipment = receipt.shipment

        Putaway putaway = new Putaway()
        putaway.origin = shipment.destination
        putaway.destination = shipment.destination
        putaway.putawayNumber = orderIdentifierService.generate(new Order())
        putaway.putawayAssignee = shipment.createdBy
        putaway.putawayStatus = PutawayStatus.PENDING

        receipt.receiptItems.each { ReceiptItem receiptItem ->
            if (!receiptItem.binLocation?.supports(ActivityCode.INBOUND_SORTATION)) {
                log.info"Receipt item destination does not support ${ActivityCode.INBOUND_SORTATION} activity code"
                return
            }

            PutawayItem putawayItem = new PutawayItem()
            putawayItem.product = receiptItem.product
            putawayItem.inventoryItem = receiptItem.inventoryItem
            putawayItem.quantity = receiptItem.quantityReceived
            putawayItem.recipient = receiptItem.recipient
            putawayItem.currentFacility = shipment.destination
            putawayItem.currentLocation = receiptItem.binLocation
            putawayItem.putawayLocation = receiptItem.product.getInventoryLevel(shipment.destination.id)?.preferredBinLocation
            putawayItem.putawayStatus = PutawayStatus.PENDING
            putaway.putawayItems.add(putawayItem)
            putawayService.savePutaway(putaway)
        }
    }
}
