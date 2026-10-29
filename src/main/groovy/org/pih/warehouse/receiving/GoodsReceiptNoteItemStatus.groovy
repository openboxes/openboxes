package org.pih.warehouse.receiving

/**
 * The receiving status of a shipment item, mirroring the statuses of the receiving workflow (getReceivingRowStatus.js).
 */
enum GoodsReceiptNoteItemStatus {
    COMPLETE,
    REMAINING,
    OVER,
    CANCELED
}
