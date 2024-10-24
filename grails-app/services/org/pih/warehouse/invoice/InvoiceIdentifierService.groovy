package org.pih.warehouse.invoice

import org.pih.warehouse.core.IdentifierService

class InvoiceIdentifierService extends IdentifierService<Invoice> {

    @Override
    String getIdentifierName() {
        return "invoice"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        return Invoice.countByInvoiceNumber(id)
    }
}
