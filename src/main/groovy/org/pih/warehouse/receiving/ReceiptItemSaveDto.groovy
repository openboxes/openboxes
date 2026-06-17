package org.pih.warehouse.receiving

/**
 * A receipt item returned in response to a save (create/update) batch. Extends {@link ReceiptItemDto} with the
 * client-side row identifier so that the client can correlate each returned line with the row it sent.
 */
class ReceiptItemSaveDto extends ReceiptItemDto {

    // Client-side identifier of the row (e.g. "temp-12345") echoed back so the client can match request to response.
    String rowId

    static ReceiptItemSaveDto from(ReceiptItem receiptItem, String rowId) {
        ReceiptItemDto base = ReceiptItemDto.from(receiptItem)
        if (!base) {
            return null
        }
        return new ReceiptItemSaveDto(
                id: base.id,
                receiptId: base.receiptId,
                shipmentItemId: base.shipmentItemId,
                recipient: base.recipient,
                productLot: base.productLot,
                receivingLocation: base.receivingLocation,
                quantityReceived: base.quantityReceived,
                quantityCanceled: base.quantityCanceled,
                comment: base.comment,
                rowId: rowId,
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return super.asResponseBody() + [rowId: rowId]
    }
}
