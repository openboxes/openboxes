package org.pih.warehouse.receiving

/**
 * A receipt item returned in response to a save (create/update) batch. Extends {@link ReceiptItemDto} with the
 * client-side row identifier so that the client can correlate each returned line with the row it sent.
 */
class ReceiptItemSaveDto extends ReceiptItemDto {

    // Client-side identifier of the row (e.g. "temp-12345") echoed back so the client can match request to response.
    String rowId

    static ReceiptItemSaveDto from(ReceiptItem receiptItem, String rowId) {
        if (!receiptItem) {
            return null
        }
        ReceiptItemSaveDto dto = new ReceiptItemSaveDto()
        dto.populate(receiptItem)
        dto.rowId = rowId
        return dto
    }

    @Override
    Map<String, Object> asResponseBody() {
        return super.asResponseBody() + [rowId: rowId]
    }
}
