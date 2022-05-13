package org.pih.warehouse.api

enum StockTransferDirection {

    INBOUND('Inbound'),
    OUTBOUND('Outbound')

    String name

    StockTransferDirection(String name) { this.name = name }

    static list() {
        [INBOUND, OUTBOUND]
    }
}
