package org.pih.warehouse.reporting

import java.math.RoundingMode

class InventoryAccuracyResult implements Serializable {
    Integer accurateCount
    Integer totalCount

    BigDecimal getAccuracyPercentage() {
        if (!totalCount) {
            return 0
        }
        return (accurateCount / totalCount * 100).setScale(2, RoundingMode.HALF_UP) ?: 0.0
    }
}
