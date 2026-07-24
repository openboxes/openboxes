package org.pih.warehouse.receiving

import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.validation.ObjectValidatable
import org.pih.warehouse.shipping.Shipment

/**
 * Pulls together all the receipt items (including pending ones) associated with a specific shipment item
 * for the purpose of determining the shipment item's current state of receiving.
 */
class ShipmentReceivingSummaryCommand implements ObjectValidatable {

    Shipment shipment
    ReceiptGroup group = ReceiptGroup.SHIPMENT_ITEM
    List<ShipmentItemReceiptStatus> receiptStatusCode
    String searchTerm
    String sort
    String order

    def beforeValidate() {
        String shipmentId = RequestContextHolder.getRequestAttributes().params?.shipmentId
        shipment = Shipment.read(shipmentId)
    }

    static constraints = {
        group(nullable: true)
        receiptStatusCode(nullable: true)
        searchTerm(nullable: true)
        sort(nullable: true)
        order(nullable: true)
    }
}
