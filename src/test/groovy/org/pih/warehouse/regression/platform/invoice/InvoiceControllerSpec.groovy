package org.pih.warehouse.regression.platform.invoice

import spock.lang.PendingFeature

import org.pih.warehouse.invoice.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.order.Order

class InvoiceControllerSpec extends Specification implements ControllerUnitTest<InvoiceController>, DataTest {

    Invoice invoice

    void setupSpec() {
        mockDomains(Invoice, Document, DocumentType, Order)
    }

    void setup() {
        controller.metaClass.warehouse = [message: { Map attrs -> attrs.code }]
        invoice = new Invoice(name: 'Invoice 1', invoiceNumber: 'INV-1').save(validate: false)

        controller.invoiceService = Mock(InvoiceService)
        controller.invoiceDataService = Mock(InvoiceDataService)
        controller.documentService = Mock(DocumentService)
        controller.prepaymentInvoiceService = Mock(PrepaymentInvoiceService)
    }

    void 'index, create and list use the react page'() {
        when:
        params.id = 'x'
        controller.index()

        then:
        response.redirectedUrl == '/invoice/create/x'

        when:
        response.reset()
        controller.create()

        then:
        view == '/common/react'

        when:
        response.reset()
        controller.list()

        then:
        view == '/common/react'
    }

    void 'show returns the invoice or redirects to the list'() {
        when:
        params.id = invoice.id
        def model = controller.show()

        then:
        model.invoiceInstance == invoice

        when:
        params.id = 'missing'
        controller.show()

        then:
        flash.message == 'default.not.found.message'
        response.redirectedUrl == '/invoice/list'
    }

    void 'rollback rolls back the invoice and reports failures'() {
        given:
        params.id = invoice.id

        when:
        controller.rollback()

        then:
        1 * controller.invoiceService.rollbackInvoice(invoice)
        flash.message == 'invoices.successfulRollback.message'
        response.redirectedUrl == "/invoice/show/${invoice.id}"

        when:
        response.reset()
        controller.rollback()

        then:
        1 * controller.invoiceService.rollbackInvoice(invoice) >> { throw new IllegalStateException('posted') }
        flash.error == 'default.not.rollback.message: posted'
        response.redirectedUrl == "/invoice/show/${invoice.id}"
    }

    void 'rollback of a missing invoice goes back to the list'() {
        given:
        params.id = 'missing'

        when:
        controller.rollback()

        then:
        0 * controller.invoiceService.rollbackInvoice(_)
        response.redirectedUrl == '/invoice/list'
    }

    void 'eraseInvoice deletes the invoice or reports why it could not'() {
        given:
        params.id = invoice.id

        when:
        controller.eraseInvoice()

        then:
        1 * controller.invoiceService.deleteInvoice(invoice)
        flash.message == 'default.deleted.message'
        response.redirectedUrl == '/invoice/list'

        when:
        response.reset()
        controller.eraseInvoice()

        then:
        1 * controller.invoiceService.deleteInvoice(invoice) >> { throw new org.springframework.dao.DataIntegrityViolationException('in use') }
        flash.message == 'default.not.deleted.message'
        response.redirectedUrl == "/invoice/show/${invoice.id}"

        when:
        response.reset()
        controller.eraseInvoice()

        then:
        1 * controller.invoiceService.deleteInvoice(invoice) >> { throw new IllegalStateException('posted') }
        flash.error == 'default.not.deleted.message: posted'

        when:
        response.reset()
        params.id = 'missing'
        controller.eraseInvoice()

        then:
        flash.message == 'default.not.found.message'
        response.redirectedUrl == '/invoice/list'
    }

    void '#action generates an invoice for the order and opens it'() {
        given:
        Order order = new Order(name: 'PO-1').save(validate: false)
        params.id = order.id

        when:
        controller."$action"()

        then:
        1 * controller.prepaymentInvoiceService."$action"(order) >> invoice
        response.redirectedUrl == "/invoice/create/${invoice.id}"

        where:
        action << ['generatePrepaymentInvoice', 'generateInvoice']
    }

    @PendingFeature(reason = 'OBAF has no return after the not-found redirect, so it still generates the invoice for a null order')
    void '#action for a missing order goes back to the list'() {
        given:
        params.id = 'missing'

        when:
        controller."$action"()

        then:
        0 * controller.prepaymentInvoiceService."$action"(_)
        flash.message == 'default.not.found.message'
        response.redirectedUrl == '/invoice/list'

        where:
        action << ['generatePrepaymentInvoice', 'generateInvoice']
    }

    void 'addDocument returns the invoice with the document types'() {
        given:
        DocumentType type = new DocumentType(name: 'Invoice').save(validate: false)

        when:
        params.id = invoice.id
        def model = controller.addDocument()

        then:
        1 * controller.documentService.getNonTemplateDocumentTypes() >> [type]
        model.invoiceInstance == invoice
        model.documentTypes == [type]

        when:
        params.id = 'missing'
        controller.addDocument()

        then:
        1 * controller.documentService.getNonTemplateDocumentTypes() >> []
        response.redirectedUrl == '/invoice/list'
    }

    void 'editDocument renders the document form'() {
        given:
        Document document = new Document(name: 'scan.pdf').save(validate: false)
        params['invoice.id'] = invoice.id
        params.id = document.id

        when:
        controller.editDocument()

        then:
        1 * controller.documentService.getNonTemplateDocumentTypes() >> []
        view == '/invoice/addDocument'
        model.documentInstance == document
        model.invoiceInstance == invoice
    }

    void 'editDocument redirects for a missing invoice'() {
        given:
        params['invoice.id'] = 'missing'

        when:
        controller.editDocument()

        then:
        1 * controller.documentService.getNonTemplateDocumentTypes() >> []
        response.redirectedUrl == '/invoice/list'
    }

    @PendingFeature(reason = 'OBAF builds the not-found message from the missing document id (NPE)')
    void 'editDocument goes back to the invoice for a missing document'() {
        given:
        params['invoice.id'] = invoice.id
        params.id = 'missing'

        when:
        controller.editDocument()

        then:
        1 * controller.documentService.getNonTemplateDocumentTypes() >> []
        flash.message == 'default.not.found.message'
        response.redirectedUrl == "/invoice/show/${invoice.id}"
    }

    void 'deleteDocument removes the document from the invoice'() {
        given:
        Document document = new Document(name: 'scan.pdf').save(validate: false)
        invoice.addToDocuments(document)
        invoice.save(validate: false)
        params['invoice.id'] = invoice.id
        params.id = document.id

        when:
        controller.deleteDocument()

        then:
        1 * controller.invoiceDataService.save(invoice) >> invoice
        invoice.documents.isEmpty()
        flash.message == 'default.updated.message'
        response.redirectedUrl == "/invoice/show/${invoice.id}"
    }

    void 'deleteDocument re-renders the invoice when saving fails'() {
        given:
        Document document = new Document(name: 'scan.pdf').save(validate: false)
        invoice.addToDocuments(document)
        invoice.save(validate: false)
        params['invoice.id'] = invoice.id
        params.id = document.id

        when:
        controller.deleteDocument()

        then:
        1 * controller.invoiceDataService.save(invoice) >> null
        view == '/invoice/show'
        model.invoiceInstance == invoice
    }

    void 'deleteDocument reports a missing invoice or document'() {
        when:
        params['invoice.id'] = 'missing'
        controller.deleteDocument()

        then:
        response.redirectedUrl == '/invoice/list'

        when:
        response.reset()
        params['invoice.id'] = invoice.id
        params.id = 'missing'
        controller.deleteDocument()

        then:
        0 * controller.invoiceDataService.save(_)
        flash.message == 'default.not.found.message'
        response.redirectedUrl == "/invoice/show/${invoice.id}"
    }
}
