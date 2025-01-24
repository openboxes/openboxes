package org.pih.warehouse.invoice

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService

@Transactional
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
