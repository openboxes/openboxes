package org.pih.warehouse.api

enum StockMovementType {

    STOCK_MOVEMENT('Stock Movement'),
    RETURN_ORDER('Return Order')

    String name

    StockMovementType(String name) { this.name = name }

    static list() {
        [STOCK_MOVEMENT, RETURN_ORDER]
    }
}
