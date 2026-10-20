package org.pih.warehouse.receiving

/**
 * The receiving status of a shipment item, mirroring the statuses of the receiving workflow (getReceivingRowStatus.js).
 */
enum GoodsReceiptNoteItemStatus {
    COMPLETE,
    REMAINING,
    OVER,
    CANCELED

    /**
     * The same precedence as the receiving workflow: a canceled remainder wins, then complete, then over/remaining.
     */
    static GoodsReceiptNoteItemStatus of(int quantityRemaining, int quantityCanceled) {
        if (quantityCanceled > 0) {
            return CANCELED
        }
        if (quantityRemaining == 0) {
            return COMPLETE
        }
        return quantityRemaining < 0 ? OVER : REMAINING
    }
}
