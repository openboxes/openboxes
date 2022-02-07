package org.pih.warehouse.api

enum StockMovementDirection {

    INBOUND('Inbound'),
    OUTBOUND('Outbound')

    String name

    StockMovementDirection(String name) { this.name = name }

    static list() {
        [INBOUND, OUTBOUND]
    }
}
