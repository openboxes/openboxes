package org.pih.warehouse.receiving

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
     * A generic map for the purpose of providing optional context around how the returned data might be grouped.
     *
     * This map is purely for convenience for the client, and is expected to support different grouping structures
     * depending on what the client needs (which it should specify via some request param).
     *
     * Because we expect this map to be used alongside the other fields of this DTO, we generally expect this map
     * to not contain complex DTOs. Instead we expect it to contain keys that can be used to look up values in the
     * other Map fields.
     *
     * For example, if grouping by container/pack level, we might have a map like:
     *
     * {
     *     "container1": {
     *         "box1: ["shipmentItemId1", "shipmentItemId2", ...],
     *         "box2: ["shipmentItemId3", "shipmentItemId4", ...],
     *     },
     *     "container2": {
     *         "box1: ["shipmentItemId5", "shipmentItemId6", ...],
     *         "box2: ["shipmentItemId7", "shipmentItemId8", ...],
     *     }
     * }
     *
     * And then the client could loop this map, fetching shipment item details from shipmentItemsById as it loops.
     *
     * This allows us to "normalize" the fields of the DTO, avoiding the need to pass the same data multiple times
     * under different fields.
     */
    Map dataGrouping = [:]

    @Override
    Map<String, Object> asResponseBody() {
        return [
                shipmentId: shipmentId,
                shipmentItemSummaryById: shipmentItemSummaryById.collectEntries { [it.key, it.value.asResponseBody()] },
                dataGrouping: dataGrouping,
        ]
    }
}
