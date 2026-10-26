package org.pih.warehouse.api

enum PickStatusCode {
    NOT_PICKED,
    PARTIALLY_PICKED,
    PICKED

    static List<PickStatusCode> list() {
        return values().toList()
    }
}
