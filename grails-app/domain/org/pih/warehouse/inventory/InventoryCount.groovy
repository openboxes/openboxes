package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryCount implements Serializable {

    String id

    Transaction transaction

    Product product

    Location facility

    Date dateRecorded

    InventoryCountTypeCode inventoryCountTypeCode

    /**
     * Counts can be created via baseline transaction + adjustment transaction together, alone adjustment or alone baseline.
     * If it's baseline + adjustment pair, the associatedTransaction would be the adjustment transaction.
     * For cases with alone adjustment or alone baseline, associatedTransaction is supposed to be null
     * This means - if transactionProfile is BASELINE_WITH_ADJUSTMENT, we expect the associatedTransaction not to be null.
     */
    Transaction associatedTransaction

    static constraints = {
        table "inventory_counts"
        version false
    }
}
