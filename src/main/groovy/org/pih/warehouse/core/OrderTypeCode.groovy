package org.pih.warehouse.core

enum OrderTypeCode {

    PURCHASE_ORDER,
    RETURN_ORDER,
    SALES_ORDER,
    TRANSFER_ORDER,
    SERVICE_ORDER

    final String value

    OrderTypeCode() {
        this.value = name()
    }

    String toString() { value }
}