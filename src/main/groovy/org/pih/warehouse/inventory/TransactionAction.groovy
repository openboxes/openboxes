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

    static List<TransactionAction> getCountActions() {
        return [
                CYCLE_COUNT,
                RECORD_STOCK,
                INVENTORY_IMPORT,
                INVENTORY_ADJUSTMENT,
        ]
    }

    static boolean isCountAction(TransactionAction action) {
        return getCountActions().contains(action)
    }
}
