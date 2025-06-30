package org.pih.warehouse.reporting

class InventoryAccuracyResult implements Serializable {
    Integer accurateCount
    Integer totalCount

    BigDecimal getAccuracyPercentage() {
        if (!totalCount) return 0
        return (accurateCount / totalCount * 100).setScale(2, BigDecimal.ROUND_HALF_UP)
    }
}
