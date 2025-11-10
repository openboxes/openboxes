package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryShrinkageResult implements Serializable {
    Product product
    Location facility
    Integer quantitySum
    BigDecimal unitPrice

    BigDecimal getTotalLoss() {
        return (quantitySum ?: 0) * (unitPrice ?: 0)
    }

    boolean isTotalAdjustmentNegative() {
        return (quantitySum ?: 0) < 0
    }
}
