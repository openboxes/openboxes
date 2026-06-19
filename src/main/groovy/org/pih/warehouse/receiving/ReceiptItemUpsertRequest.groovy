package org.pih.warehouse.receiving

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentItem

class ReceiptItemUpsertRequest implements Validateable {

    // Existing receipt item to update. When not provided, a new receipt item is created.
    ReceiptItem receiptItem

    // Shipment item this receipt item is received against.
    ShipmentItem shipmentItem

    // Client-side identifier of the row (e.g. "temp-12345"), used to correlate the request with the response.
    String rowId

    Integer quantityReceiving

    Location binLocation

    static constraints = {
        receiptItem(nullable: true)
        rowId(nullable: true)
        quantityReceiving(nullable: true)
        binLocation(nullable: true)
    }
}
