package org.pih.warehouse.receiving

/**
 * The comment of a single receipt item.
 */
class ReceiptItemCommentDto {

    String receiptItemId
    String comment

    static ReceiptItemCommentDto from(ReceiptItem receiptItem) {
        return !receiptItem ? null : new ReceiptItemCommentDto(
                receiptItemId: receiptItem.id,
                comment: receiptItem.comment,
        )
    }
}
