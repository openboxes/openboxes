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

        receipt.receiptItems.each { ReceiptItem receiptItem ->
            if (!receiptItem.binLocation?.supports(ActivityCode.INBOUND_SORTATION)) {
                log.info"Receipt item destination does not support ${ActivityCode.INBOUND_SORTATION} activity code"
                return
            }

            Putaway putaway = createPutaway(shipment)
            putaway.putawayItems.add(createPutawayItem(receiptItem, shipment))
            putawayService.savePutaway(putaway)
        }
    }

    private Putaway createPutaway(Shipment shipment) {
        new Putaway(
                origin: shipment.destination,
                destination: shipment.destination,
                putawayNumber: orderIdentifierService.generate(new Order()),
                putawayAssignee: shipment.createdBy,
                putawayStatus: PutawayStatus.PENDING
        )
    }

    private PutawayItem createPutawayItem(ReceiptItem receiptItem, Shipment shipment) {
        new PutawayItem(
                product: receiptItem.product,
                inventoryItem: receiptItem.inventoryItem,
                quantity: receiptItem.quantityReceived,
                recipient: receiptItem.recipient,
                currentFacility: shipment.destination,
                currentLocation: receiptItem.binLocation,
                putawayLocation: receiptItem.product.getInventoryLevel(shipment.destination.id)?.preferredBinLocation,
                putawayStatus: PutawayStatus.PENDING
        )
    }
}
