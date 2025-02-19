package org.pih.warehouse.inventory

import org.pih.warehouse.core.Constants

/**
 * Enumerates the different features/sources that can trigger a "snapshot style" product inventory transaction.
 */
enum ProductInventorySnapshotSource {
    CYCLE_COUNT(Constants.CYCLE_COUNT_PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)

    String transactionTypeId

    ProductInventorySnapshotSource(String transactionTypeId){
        this.transactionTypeId = transactionTypeId
    }
}
