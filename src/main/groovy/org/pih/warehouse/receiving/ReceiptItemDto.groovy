package org.pih.warehouse.receiving

import org.pih.warehouse.core.PersonDto
import org.pih.warehouse.location.LocationSimpleDto
import org.pih.warehouse.product.lot.ProductLotDto

/**
 * A simple, general purpose DTO representing a single item of a receipt.
 */
class ReceiptItemDto {

    String id
    String receiptId
    String shipmentItemId
    PersonDto recipient
    ProductLotDto productLot

    /**
     * If doing a direct putaway as a part of the receipt this will be a bin location.
     * If direct putaways are disabled, this will be an internal, temporary receiving location.
     */
    LocationSimpleDto receivingLocation

    Integer quantityReceived = 0
    Integer quantityCanceled = 0
    String comment

    static ReceiptItemDto from(ReceiptItem receiptItem) {
        return !receiptItem ? null : new ReceiptItemDto(
                id: receiptItem.id,
                receiptId: receiptItem.receiptId,
                shipmentItemId: receiptItem.shipmentItemId,
                recipient: PersonDto.from(receiptItem.recipient),
                productLot: ProductLotDto.from(receiptItem.inventoryItem),
                receivingLocation: LocationSimpleDto.from(receiptItem.binLocation),
                quantityReceived: receiptItem.quantityReceived ?: 0,
                quantityCanceled: receiptItem.quantityCanceled ?: 0,
                comment: receiptItem.comment,
        )
    }
}
