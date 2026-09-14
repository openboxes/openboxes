package org.pih.warehouse.receiving

import grails.validation.ValidationException
import org.springframework.stereotype.Component

import org.pih.warehouse.api.receiving.v2.ReceiptV2Service
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentItem

/**
 * Creates the receipt line a shipment item is received on, for the flows that put a shipment item onto a receipt:
 * starting a receipt ({@link ReceiptV2Service#startReceipt}) and syncing the lines of one
 * ({@link ReceiptSynchronizer#syncLines}). The split lines added while receiving are written by the endpoints that
 * edit a receipt instead (see {@link ReceiptV2Service#upsertReceiptItem}).
 *
 * Writes within the transaction of the caller, so it is the calling endpoint that decides what a failure rolls
 * back.
 */
@Component
class ReceiptItemFactory {

    /**
     * Creates the "original" receipt item of a shipment item: an empty (nothing received yet) line mirroring the
     * shipment item and carrying its full quantity as the quantity shipped. Its quantity received is left null
     * rather than zero: null means "nothing entered yet" (the client renders an empty input, autofills it and
     * filters on it) while a zero is a quantity the user deliberately entered. Nulls only ever live on a pending
     * receipt - completing one writes them out as zeros (see
     * {@link ReceiptV2Service#zeroOutEmptyReceivedQuantities}).
     * Exactly one original line exists per still-receivable shipment item on a receipt - it is created when the
     * receipt is started, cannot be deleted (the batch update endpoint rejects deleting it) and is the only line
     * whose remainder can be canceled on completion. Shipment items already fully consumed by previous receipts
     * get no line: it would only produce zero-quantity transaction entries on completion, and the receiving client
     * marks an item as completed precisely by it having nothing pending on the current receipt.
     * Lines added while receiving are split lines instead: they are flagged with isSplitItem and carry a quantity
     * shipped of zero, so they never factor into the cancel-remaining math.
     */
    ReceiptItem createReceiptItemFromShipmentItem(
            Receipt receipt, ShipmentItem shipmentItem, Location receivingBin) {
        ReceiptItem receiptItem = new ReceiptItem(
                product: shipmentItem.product,
                inventoryItem: shipmentItem.inventoryItem,
                lotNumber: shipmentItem.lotNumber,
                expirationDate: shipmentItem.expirationDate,
                recipient: shipmentItem.recipient,
                quantityShipped: shipmentItem.quantity,
                quantityReceived: null,
                isSplitItem: Boolean.FALSE,
                binLocation: receivingBin,
                sortOrder: shipmentItem.receiptItems?.size() ?: 0,
        )

        receipt.addToReceiptItems(receiptItem)
        shipmentItem.addToReceiptItems(receiptItem)

        if (!receiptItem.save()) {
            throw new ValidationException("Receipt item is invalid", receiptItem.errors)
        }

        return receiptItem
    }
}
