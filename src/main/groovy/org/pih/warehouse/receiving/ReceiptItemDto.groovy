package org.pih.warehouse.receiving

import org.pih.warehouse.core.PersonDto
import org.pih.warehouse.core.http.ResponseBodyFormattable
import org.pih.warehouse.location.LocationSimpleDto
import org.pih.warehouse.product.lot.ProductLotDto

/**
 * A simple, general purpose DTO representing a single item of a receipt.
 */
class ReceiptItemDto implements ResponseBodyFormattable {

    String id
    String receiptId
    String shipmentItemId
    PersonDto recipient
    ProductLotDto productLot

    /**
     * If doing a direct putaway as a part of the receipt this will be a a bin location.
     * If direct putaways are disabled, this will be an internal, temporary receiving location.
     */
    LocationSimpleDto receivingLocation

    Integer quantityReceived = 0
    Integer quantityCanceled = 0
    String comment

    static ReceiptItemDto from(ReceiptItem receiptItem) {
        if (!receiptItem) {
            return null
        }
        ReceiptItemDto dto = new ReceiptItemDto()
        dto.populate(receiptItem)
        return dto
    }

    /**
     * Copies the shared receipt item fields onto this instance. Subclasses can reuse this to fill the common fields
     * instead of re-listing them, then add their own.
     */
    protected void populate(ReceiptItem receiptItem) {
        id = receiptItem.id
        receiptId = receiptItem.receiptId
        shipmentItemId = receiptItem.shipmentItemId
        recipient = PersonDto.from(receiptItem.recipient)
        productLot = ProductLotDto.from(receiptItem.inventoryItem)
        receivingLocation = LocationSimpleDto.from(receiptItem.binLocation)
        quantityReceived = receiptItem.quantityReceived ?: 0
        quantityCanceled = receiptItem.quantityCanceled ?: 0
        comment = receiptItem.comment
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                id: id,
                receiptId: receiptId,
                shipmentItemId: shipmentItemId,
                recipient: recipient,
                productLot: productLot?.asResponseBody(),
                binLocation: receivingLocation?.asResponseBody(),
                quantityReceived: quantityReceived,
                quantityCanceled: quantityCanceled,
                comment: comment,
        ]
    }
}
