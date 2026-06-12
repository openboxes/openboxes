package org.pih.warehouse.receiving

import org.pih.warehouse.core.validation.ObjectValidatable
import org.pih.warehouse.shipping.Shipment

/**
 * Pulls together all the receipt items (including pending ones) associated with a specific shipment item
 * for the purpose of determining the shipment item's current state of receiving.
 */
class ShipmentReceivingSummaryCommand implements ObjectValidatable {

    Shipment shipment
    ReceiptGrouping grouping = ReceiptGrouping.NONE

    static constraints = {
        grouping(nullable: true)
    }
}
