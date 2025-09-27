package org.pih.warehouse.inboundSortation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem

@Transactional
class InboundSortationService {

    def orderIdentifierService
    def putawayService
    def slottingService

    void createPutawayOrdersFromReceipt(Receipt receipt) {
        receipt.receiptItems.each { ReceiptItem receiptItem ->
            if (!receiptItem.binLocation?.supports(ActivityCode.INBOUND_SORTATION)) {
                log.warn "Receipt item destination does not support ${ActivityCode.INBOUND_SORTATION} activity code. " +
                        "Putaway order will not be created."
                return
            }

            PutawayContext putawayContext = createPutawayContext(receiptItem)
            List<PutawayResult> results = slottingService.execute(putawayContext)
            results.each { PutawayResult result ->
                if (result.quantity > 0) {
                    Putaway putaway = createPutaway(putawayContext, receipt.shipment?.createdBy)
                    putaway.putawayItems.add(createPutawayItem(result))
                    putawayService.savePutaway(putaway)
                }
            }
        }
    }

    private PutawayContext createPutawayContext(ReceiptItem receiptItem) {
        def shipment = receiptItem.receipt.shipment
        new PutawayContext(
                facility: shipment.destination,
                product: receiptItem.product,
                inventoryItem: receiptItem.inventoryItem,
                lotNumber: receiptItem.inventoryItem.lotNumber,
                expirationDate: receiptItem.inventoryItem.expirationDate,
                currentBinLocation: receiptItem.binLocation,
                preferredBin: receiptItem.product.getInventoryLevel(shipment.destination.id)?.preferredBinLocation,
                quantity: receiptItem.quantityReceived
        )
    }

    private Putaway createPutaway(PutawayContext putawayContext, User createdBy) {
        new Putaway(
                origin: putawayContext.facility,
                destination: putawayContext.facility,
                putawayNumber: orderIdentifierService.generate(new Order()),
                putawayAssignee: createdBy,
                putawayStatus: PutawayStatus.PENDING
        )
    }

    private PutawayItem createPutawayItem(PutawayResult task) {
        new PutawayItem(
                product: task.product,
                inventoryItem: task.inventoryItem,
                quantity: task.quantity,
                currentFacility: task.facility,
                currentLocation: task.location,
                putawayLocation: task.destination,
                putawayStatus: PutawayStatus.PENDING
        )
    }
}
