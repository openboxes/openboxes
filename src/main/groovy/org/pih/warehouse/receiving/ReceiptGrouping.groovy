package org.pih.warehouse.receiving

/**
 * Enumerates the different types of data grouping that we can perform when fetching data relating to Receipts.
 */
enum ReceiptGrouping {
    /**
     * No grouping is required.
     */
    NONE,

    /**
     * Groups data by {@link org.pih.warehouse.shipping.Container}.
     */
    PACK_LEVEL,
}