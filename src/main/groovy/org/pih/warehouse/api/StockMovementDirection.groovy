package org.pih.warehouse.api

enum StockMovementDirection {

    INBOUND('Inbound'),
    OUTBOUND('Outbound'),
    INTERNAL('Internal')

    String name

    StockMovementDirection(String name) { this.name = name }

    static list() {
        [INBOUND, OUTBOUND, INTERNAL]
    }
}
