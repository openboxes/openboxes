package org.pih.warehouse.inventory

import groovy.transform.EqualsAndHashCode

/**
 * A key on [product id + bin location id], used by AutoIssuanceTransactionReportService to look up
 * per-product-per-bin values (last counted date, quantity on hand) that are computed separately from
 * the report's grouped rows.
 */
@EqualsAndHashCode
class ProductAndBinKey {

    String productId
    String binLocationId

    ProductAndBinKey(String productId, String binLocationId) {
        this.productId = productId
        this.binLocationId = binLocationId
    }
}
