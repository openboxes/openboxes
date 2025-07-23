package org.pih.warehouse.inventory

enum TransactionProfile {
    BASELINE_WITH_ADJUSTMENT,
    ADJUSTMENT_WITHOUT_BASELINE,
    BASELINE_WITHOUT_ADJUSTMENT

    @Override
    String toString() {
        return name()
    }
}
