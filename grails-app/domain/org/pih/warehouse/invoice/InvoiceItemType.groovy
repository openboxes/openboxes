package org.pih.warehouse.invoice

enum InvoiceItemType {

    REGULAR('Regular'),
    INVERSE('Inverse')

    String type

    InvoiceItemType(String type) {
        this.type = type
    }
}
