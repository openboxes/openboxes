package org.pih.warehouse.receiving

import org.pih.warehouse.core.OrderedDataGrouping
import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * Pulls together all the receipts (including pending ones) associated with a shipment for the purpose of determining
 * the shipment's current state of receiving.
 */
class ShipmentReceivingSummaryDto implements ResponseBodyFormattable {

    String shipmentId

    /**
     * A map of shipment item receiving summaries, keyed on shipment item id.
     *
     * Is a Map to make it convenient for the client in case it wants to rely on the dataGrouping
     */
    Map<String, ShipmentItemReceivingSummaryDto> shipmentItemSummaryById = [:]

    /**
     * Defines a suggested ordered grouping structure for displaying the summary data.
     * Exists purely for client convenience.
     */
    OrderedDataGrouping dataGrouping

    @Override
    Map<String, Object> asResponseBody() {
        return [
                shipmentId: shipmentId,
                shipmentItemSummaryById: shipmentItemSummaryById.collectEntries { [it.key, it.value.asResponseBody()] },
                dataGrouping: dataGrouping.asResponseBody(),
        ]
    }
}
