package org.pih.warehouse.inventory

enum InventoryCountTypeCode {
    BASELINE_ADJUSTMENT,
    ADJUSTMENT,
    BASELINE

    @Override
    String toString() {
        return name()
    }
}
