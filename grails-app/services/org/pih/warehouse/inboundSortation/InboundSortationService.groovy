package org.pih.warehouse.inboundSortation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.putaway.PutawayTask
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
                    PutawayItem putawayItem = createPutawayItem(result)
                    putawayItem.receipt = receiptItem.receipt
                    putawayItem.receiptItem = receiptItem
                    putaway.putawayItems.add(putawayItem)
                    putawayService.savePutaway(putaway)
                }
            }
        }
    }

    void rerunPutawayStrategy(PutawayTask task) {
        // 1. Validate PENDING only
        if (task.status != PutawayTaskStatus.PENDING) {
            throw new IllegalStateException("Can only rerun strategy on PENDING putaway tasks")
        }

        // 2. Capture context from existing order item (before deletion)
        OrderItem orderItem = OrderItem.get(task.putawayOrderItem.id)
        Order order = orderItem.order
        Receipt receipt = orderItem.receipt
        ReceiptItem receiptItem = orderItem.receiptItem
        User createdBy = order.orderedBy

        // 3. Build PutawayContext
        PutawayContext context = new PutawayContext(
                facility: task.facility,
                product: task.product,
                inventoryItem: task.inventoryItem,
                lotNumber: task.inventoryItem.lotNumber,
                expirationDate: task.inventoryItem.expirationDate,
                currentBinLocation: task.location,
                preferredBin: task.product.getInventoryLevel(task.facility.id)?.preferredBinLocation,
                quantity: task.quantity.intValue()
        )

        // 4. Delete old putaway order (rollbackAndDelete handles PENDING correctly — no-op rollback + delete)
        task.discard()
        putawayService.rollbackAndDelete(Putaway.createFromOrder(order))

        // 5. Execute strategy chain
        List<PutawayResult> results = slottingService.execute(context)

        // 6. Create new putaway orders from results (same pattern as createPutawayOrdersFromReceipt)
        results.each { PutawayResult result ->
            if (result.quantity > 0) {
                Putaway putaway = createPutaway(context, createdBy)
                PutawayItem putawayItem = createPutawayItem(result)
                putawayItem.receipt = receipt
                putawayItem.receiptItem = receiptItem
                putaway.putawayItems.add(putawayItem)
                putawayService.savePutaway(putaway)
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
                containerLocation: task.container,
                putawayStatus: PutawayStatus.PENDING,
                comment: task.comment
        )
    }
}
