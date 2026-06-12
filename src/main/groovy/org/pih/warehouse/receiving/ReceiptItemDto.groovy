package org.pih.warehouse.receiving

import org.pih.warehouse.core.http.ResponseBodyFormattable
import org.pih.warehouse.location.BinLocationDto
import org.pih.warehouse.product.lot.ProductLotDto

/**
 * A simple, general purpose DTO representing a single item of a receipt.
 */
class ReceiptItemDto implements ResponseBodyFormattable {

    String id
    String receiptId
    String shipmentItemId
    String recipientId
    ProductLotDto productLot
    BinLocationDto binLocation
    Integer quantityReceived = 0
    Integer quantityCanceled = 0
    String comment

    static ReceiptItemDto from(ReceiptItem receiptItem) {
        return !receiptItem ? null : new ReceiptItemDto(
                id: receiptItem.id,
                receiptId: receiptItem.receiptId,
                shipmentItemId: receiptItem.shipmentItemId,
                recipientId: receiptItem.recipientId,
                productLot: ProductLotDto.from(receiptItem.inventoryItem),
                binLocation: BinLocationDto.from(receiptItem.binLocation),
                quantityReceived: receiptItem.quantityReceived,
                quantityCanceled: receiptItem.quantityCanceled,
                comment: receiptItem.comment,
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                id: id,
                receiptId: receiptId,
                shipmentItemId: shipmentItemId,
                recipientId: recipientId,
                productLot: productLot?.asResponseBody(),
                binLocation: binLocation?.asResponseBody(),
                quantityReceived: quantityReceived,
                quantityCanceled: quantityCanceled,
                comment: comment,
        ]
    }
}
