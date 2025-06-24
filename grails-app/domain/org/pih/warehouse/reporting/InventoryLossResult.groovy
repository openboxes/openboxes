package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryLossResult implements Serializable {
    Product product
    Location facility
    Integer quantitySum
    BigDecimal unitPrice

    BigDecimal getTotalLoss() {
        (quantitySum ?: 0) * (unitPrice ?: 0)
    }

    boolean isTotalAdjustmentNegative() {
        (quantitySum ?: 0) < 0
    }
}
