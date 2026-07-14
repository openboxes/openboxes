package org.pih.warehouse.receiving

import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * The comment of a single receipt item.
 */
class ReceiptItemCommentDto implements ResponseBodyFormattable {

    String receiptItemId
    String comment

    static ReceiptItemCommentDto from(ReceiptItem receiptItem) {
        return !receiptItem ? null : new ReceiptItemCommentDto(
                receiptItemId: receiptItem.id,
                comment: receiptItem.comment,
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                receiptItemId: receiptItemId,
                comment: comment,
        ]
    }
}
