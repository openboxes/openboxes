package org.pih.warehouse.receiving

import org.pih.warehouse.core.OrderedDataGroup

/**
 * Pulls together all the receipts (including pending ones) associated with a shipment for the purpose of determining
 * the shipment's current state of receiving.
 */
class ShipmentReceivingSummaryDto {

    String shipmentId

    /**
     * A map of shipment item receiving summaries, keyed on shipment item id.
     *
     * Is a Map to make it convenient for the client in case it wants to rely on the shipmentItemsGrouped ordering.
     */
    Map<String, ShipmentItemReceivingSummaryDto> shipmentItemSummaryById = [:]

    /**
     * Defines a suggested ordered grouping for displaying the shipment item summary data.
     * Exists purely for client convenience.
     */
    OrderedDataGroup shipmentItemsGrouped
}
