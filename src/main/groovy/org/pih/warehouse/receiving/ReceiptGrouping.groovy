package org.pih.warehouse.receiving

/**
 * Enumerates the different types of data grouping that we can perform when fetching data relating to Receipts.
 */
enum ReceiptGrouping {
    /**
     * Groups data by {@link org.pih.warehouse.shipping.ShipmentItem}.
     */
    SHIPMENT_ITEM,

    /**
     * Groups data by {@link org.pih.warehouse.shipping.Container}.
     */
    PACK_LEVEL,
}
