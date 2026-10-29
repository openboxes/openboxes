package org.pih.warehouse.receiving

import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Everything the Goods Receipt Note document renders for a shipment.
 */
class GoodsReceiptNoteDto {

    String shipmentNumber
    String description
    ShipmentStatusCode statusCode
    String originName
    String destinationName
    Date dateShipped
    Date lastReceiptDate

    List<GoodsReceiptNoteReceiptDto> receipts = []

    /**
     * One entry per shipment item of the shipment (received or not), in shipment order.
     */
    List<GoodsReceiptNoteOriginalItemDto> items = []

    /**
     * A column per receipt only makes sense next to the total when there is more than one receipt.
     */
    boolean isReceiptColumnsDisplayed() {
        return receipts.size() > 1
    }
}
