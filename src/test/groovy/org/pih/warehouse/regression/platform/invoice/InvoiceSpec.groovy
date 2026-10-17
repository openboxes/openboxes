package org.pih.warehouse.regression.platform.invoice

import org.pih.warehouse.invoice.*
import grails.testing.gorm.DataTest
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class InvoiceSpec extends Specification implements TagLibUnitTest<ApplicationTagLib>, DataTest {

    InvoiceType prepaymentType
    InvoiceType regularType

    void setupSpec() {
        mockDomains(Invoice, InvoiceItem, InvoiceType, Order, OrderItem, OrderAdjustment, Shipment, ShipmentItem,
                ReferenceNumber, ReferenceNumberType, Organization, Document, UnitOfMeasure)
        mockTagLib(ApplicationTagLib)
    }

    void setup() {
        prepaymentType = new InvoiceType(name: 'Prepayment', code: InvoiceTypeCode.PREPAYMENT_INVOICE).save(validate: false)
        regularType = new InvoiceType(name: 'Invoice', code: InvoiceTypeCode.INVOICE).save(validate: false)
    }

    void 'the status follows the latest date that is set'() {
        expect:
        new Invoice(datePaid: paid, datePosted: posted, dateSubmitted: submitted).status == expected

        where:
        paid       | posted     | submitted  || expected
        new Date() | new Date() | new Date() || InvoiceStatus.PAID
        null       | new Date() | new Date() || InvoiceStatus.POSTED
        null       | null       | new Date() || InvoiceStatus.SUBMITTED
        null       | null       | null       || InvoiceStatus.PENDING
    }

    void 'totals add up the item amounts, prepayment totals only for prepayment invoices'() {
        given:
        Invoice prepayment = new Invoice(invoiceType: prepaymentType, invoiceItems: [
                new InvoiceItem(amount: 10.5), new InvoiceItem(amount: 4.5), new InvoiceItem()] as Set)
        Invoice regular = new Invoice(invoiceType: regularType, invoiceItems: [new InvoiceItem(amount: 3)] as Set)

        expect:
        prepayment.totalValue == 15
        prepayment.totalPrepaymentValue == 15
        prepayment.isPrepaymentInvoice
        !prepayment.isRegularInvoice
        regular.totalValue == 3
        regular.totalPrepaymentValue == 0
        regular.isRegularInvoice
        new Invoice().totalValue == 0
    }

    void 'the vendor invoice number is the reference number of the vendor invoice type'() {
        given:
        ReferenceNumberType vendorType = new ReferenceNumberType(name: 'Vendor invoice number')
        vendorType.id = Constants.VENDOR_INVOICE_NUMBER_TYPE_ID
        vendorType.save(validate: false)
        ReferenceNumberType otherType = new ReferenceNumberType(name: 'Other')
        otherType.id = 'OTHER'
        otherType.save(validate: false, flush: true)
        ReferenceNumber vendorNumber = new ReferenceNumber(identifier: 'V-100', referenceNumberType: vendorType)
        Invoice invoice = new Invoice(referenceNumbers: [
                new ReferenceNumber(identifier: 'X', referenceNumberType: otherType), vendorNumber] as Set)

        expect:
        invoice.vendorInvoiceNumber.is(vendorNumber)
        invoice.getReferenceNumber('OTHER').identifier == 'X'
        invoice.getReferenceNumber('MISSING') == null
        new Invoice(referenceNumbers: [] as Set).vendorInvoiceNumber == null
    }

    void 'orders, shipments and prepayment invoices are found through the invoice items'() {
        given:
        Order order = new Order(orderNumber: 'PO-1').save(validate: false)
        OrderItem orderItem = new OrderItem(order: order).save(validate: false)
        order.orderItems = [orderItem] as Set
        Shipment shipment = new Shipment(name: 'S').save(validate: false)
        ShipmentItem shipmentItem = new ShipmentItem(shipment: shipment, orderItems: [orderItem] as Set).save(validate: false)
        OrderAdjustment adjustment = new OrderAdjustment(order: order).save(validate: false)

        Invoice prepayment = new Invoice(invoiceNumber: 'PRE', invoiceType: prepaymentType).save(validate: false)
        InvoiceItem prepaymentItem = new InvoiceItem(invoice: prepayment, amount: 5, orderItems: [orderItem] as Set,
                shipmentItems: [] as Set, orderAdjustments: [] as Set)
                .save(validate: false)
        prepayment.invoiceItems = [prepaymentItem] as Set

        Invoice finalInvoice = new Invoice(invoiceNumber: 'FIN', invoiceType: regularType).save(validate: false)
        InvoiceItem byShipment = new InvoiceItem(invoice: finalInvoice, id: 'b', shipmentItems: [shipmentItem] as Set,
                orderItems: [] as Set, orderAdjustments: [] as Set)
        InvoiceItem byAdjustment = new InvoiceItem(invoice: finalInvoice, id: 'a', inverse: true,
                orderAdjustments: [adjustment] as Set, orderItems: [] as Set, shipmentItems: [] as Set)
        finalInvoice.invoiceItems = [byShipment, byAdjustment] as Set
        orderItem.invoiceItems = [prepaymentItem] as Set
        shipmentItem.invoiceItems = [byShipment] as Set
        adjustment.invoiceItems = [byAdjustment] as Set
        order.orderAdjustments = [adjustment] as Set

        expect:
        finalInvoice.orders == [order]
        finalInvoice.shipments == [shipment]
        finalInvoice.prepaymentInvoices == [prepayment]
        finalInvoice.prepaymentItems == [prepaymentItem]
        finalInvoice.hasPrepaymentInvoice
        finalInvoice.hasRegularInvoice
        prepayment.prepaymentInvoices == []
        prepayment.prepaymentItems == []
        finalInvoice.sortedInvoiceItems*.inverse == [false, true]
    }

    void 'order documents are collected once each and included in the json'() {
        given:
        Organization vendor = new Organization(code: 'ACME', name: 'Acme').save(validate: false)
        Document contract = new Document(name: 'contract.pdf').save(validate: false)
        Document own = new Document(name: 'scan.pdf').save(validate: false)
        Order order = new Order(orderNumber: 'PO-1', documents: [contract] as Set).save(validate: false)
        OrderItem first = new OrderItem(order: order).save(validate: false)
        OrderItem second = new OrderItem(order: order).save(validate: false)
        Invoice invoice = new Invoice(invoiceNumber: 'INV-1', name: 'March', party: vendor, partyFrom: vendor,
                dateInvoiced: new GregorianCalendar(2026, Calendar.MARCH, 5).time, invoiceType: regularType,
                invoiceItems: [new InvoiceItem(amount: 2, orderItems: [first] as Set),
                               new InvoiceItem(amount: 3, orderItems: [second] as Set)] as Set)

        when:
        Map json = invoice.toJson()

        then:
        invoice.orderDocuments*.name == ['contract.pdf']
        json.invoiceNumber == 'INV-1'
        json.dateInvoiced == '03/05/2026'
        json.vendor == vendor.id
        json.vendorName == 'ACME Acme'
        json.partyFrom == vendor
        json.totalCount == 2
        json.totalValue == 5
        json.invoiceType == 'INVOICE'
        json.status == 'PENDING'
        !json.isPrepaymentInvoice
        json.documents*.name == ['contract.pdf']

        when:
        invoice.documents = [own] as Set

        then:
        invoice.toJson().documents*.name == ['scan.pdf', 'contract.pdf']
    }

    void 'the normalized total needs no conversion in the default currency'() {
        given:
        config.openboxes.locale.defaultCurrencyCode = 'USD'
        Invoice invoice = new Invoice(currencyUom: new UnitOfMeasure(code: 'USD'),
                invoiceItems: [new InvoiceItem(amount: 7)] as Set)

        expect:
        invoice.totalValueNormalized == 7
    }
}
