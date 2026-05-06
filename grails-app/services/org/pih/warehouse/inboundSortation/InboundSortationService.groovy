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
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

@Transactional
class InboundSortationService {

    def orderIdentifierService
    def putawayService
    def slottingService

    /**
     * Returns an error message if any shipment item has an unresolvable backorder reference,
     * or null if all backorder references can be matched. Used to short-circuit the receive
     * flow before status changes / receipt creation happen.
     */
    String validateBackorderReferences(Shipment shipment) {
        for (ShipmentItem item : shipment.shipmentItems) {
            if (!item.backorderReference || item.backorderItem) {
                continue
            }
            Requisition backorder = Requisition.findByRequestNumber(item.backorderReference)
            if (!backorder) {
                return "Auto-receive paused: backorder '${item.backorderReference}' was not found in the system. " +
                        "Will retry once the backorder is available."
            }
            boolean hasMatch = backorder.requisitionItems.any {
                it.product == item.product && it.quantity <= item.quantity && !it.isAllocated()
            }
            if (!hasMatch) {
                return "Auto-receive paused: backorder '${item.backorderReference}' has no available line for product '${item.product}' " +
                        "with quantity ${item.quantity}. Will retry once a matching line is available."
            }
        }
        return null
    }

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

        // 2. Build PutawayContext from existing task
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

        // 3. Execute strategy chain
        List<PutawayResult> results = slottingService.execute(context)
        PutawayResult result = results?.find { it.quantity > 0 }

        // 4. Update existing order item with new strategy results
        OrderItem orderItem = OrderItem.get(task.putawayOrderItem.id)
        orderItem.destinationBinLocation = result?.destination
        orderItem.containerLocation = result?.container
        orderItem.description = result?.comment
        orderItem.save(failOnError: true)
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
                quantity: receiptItem.quantityReceived,
                backorderReference: receiptItem.shipmentItem.backorderReference,
                backorderItem: receiptItem.shipmentItem.backorderItem,
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
