package org.pih.warehouse.regression.platform.invoice

import spock.lang.PendingFeature

import org.pih.warehouse.invoice.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

/**
 * InvoiceService.updateItems fails, instead of returning quietly, when the invoice cannot be saved: the API answers
 * 400 and the transaction keeps nothing of the change.
 */
class InvoiceServiceUpdateItemsSpec extends Specification implements ServiceUnitTest<InvoiceService>, DataTest {

    void setupSpec() {
        mockDomains(Invoice, InvoiceItem)
    }

    @PendingFeature(reason = 'OBAF updateItems ignores a failed invoice.save() instead of throwing ValidationException')
    void 'an invoice that does not validate is refused'() {
        given: 'an invoice without its required number'
        Invoice invoice = new Invoice(name: 'No number')

        when:
        service.updateItems(invoice, [])

        then:
        thrown(ValidationException)
    }

    void 'a valid invoice is saved and returned'() {
        given:
        Invoice invoice = new Invoice(invoiceNumber: 'INV-1', name: 'Numbered')

        expect:
        service.updateItems(invoice, []) == invoice
        invoice.id != null
    }
}
