package org.pih.warehouse.api

enum DocumentGroupCode {

    EXPORT('Export'),
    INVOICE_TEMPLATE('Invoice Template'),
    PICKLIST('Pick list'),
    PACKING_LIST('Packing List'),
    CERTIFICATE_OF_DONATION('Certificate of Donation'),
    DELIVERY_NOTE('Delivery Note'),
    GOODS_RECEIPT_NOTE('Goods Receipt Note'),

    final String description

    DocumentGroupCode(String description) {
        this.description = description
    }

    static list() {
        return [EXPORT, INVOICE_TEMPLATE, PICKLIST, PACKING_LIST, CERTIFICATE_OF_DONATION, DELIVERY_NOTE, GOODS_RECEIPT_NOTE]
    }

}
