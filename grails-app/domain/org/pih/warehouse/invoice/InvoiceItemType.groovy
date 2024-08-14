package org.pih.warehouse.invoice

enum InvoiceItemType {

    INVERSE('Inverse'),
    PREPAYMENT('Prepayment'),
    REGULAR('Regular')

    String type

    String toString() {
        return name()
    }

    InvoiceItemType(String type) {
        this.type = type
    }
}
