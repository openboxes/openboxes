package org.pih.warehouse.inventory

enum TransactionAction {
    CYCLE_COUNT,
    RECORD_STOCK,
    INVENTORY_IMPORT,
    INVENTORY_ADJUSTMENT

    @Override
    String toString() {
        return name()
    }
}
