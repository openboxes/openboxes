package org.pih.warehouse.invoice

enum InvoiceItemType {

    INVERSE('Inverse'),
    PREPAYMENT('Prepayment'),
    REGULAR('Regular')

    String type

    InvoiceItemType(String type) {
        this.type = type
    }
}
