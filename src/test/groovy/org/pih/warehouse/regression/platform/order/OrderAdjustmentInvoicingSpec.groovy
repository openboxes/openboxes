package org.pih.warehouse.regression.platform.order

import org.pih.warehouse.order.*
import spock.lang.Specification

import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.OrderAdjustmentInvoiceStatus
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode

/**
 * Invoice arithmetic of order adjustments, computed from real invoice items.
 */
class OrderAdjustmentInvoicingSpec extends Specification {

    static InvoiceType regular = new InvoiceType(code: InvoiceTypeCode.INVOICE)
    static InvoiceType prepayment = new InvoiceType(code: InvoiceTypeCode.PREPAYMENT_INVOICE)

    private static InvoiceItem item(BigDecimal unitPrice, Map args = [:]) {
        Invoice invoice = new Invoice(invoiceType: args.type ?: regular, datePosted: args.posted ? new Date() : null)
        return new InvoiceItem(invoice: invoice, unitPrice: unitPrice, inverse: args.inverse ?: false)
    }

    private static OrderAdjustment adjustment(Date created, Date updated) {
        OrderAdjustment adjustment = new OrderAdjustment()
        adjustment.dateCreated = created
        adjustment.lastUpdated = updated
        return adjustment
    }

    private static Order placedOrder() {
        return new Order(status: OrderStatus.PLACED)
    }

    void 'total adjustments come from the amount or a percentage of the item or order subtotal'() {
        given:
        OrderItem orderItem = Stub(OrderItem) { getSubtotal() >> 200 }
        Order order = Stub(Order) { getSubtotal() >> 1000 }

        expect:
        new OrderAdjustment(amount: 15).totalAdjustments == 15
        new OrderAdjustment(percentage: 10, orderItem: orderItem).totalAdjustments == 20
        new OrderAdjustment(percentage: 5, order: order).totalAdjustments == 50
        new OrderAdjustment().totalAdjustments == 0
    }

    void 'invoiced, posted and inverse unit prices only count the matching invoice items'() {
        given:
        OrderAdjustment adjustment = new OrderAdjustment(amount: 100, invoiceItems: [
                item(30, [posted: true]),
                item(20),
                item(-10, [inverse: true, posted: true]),
                item(40, [type: prepayment, posted: true]),
        ] as Set)

        expect:
        adjustment.invoicedUnitPrice == 50
        adjustment.unitPriceOnPostedInvoices == 30
        adjustment.inversedUnitPrice == -10
        adjustment.totalAmountNotInvoiced == 70
        adjustment.postedPurchaseInvoiceItems*.unitPrice.sort() == [-10, 30]
        !adjustment.isFullyInvoiced
        adjustment.unitPriceAvailableToInvoice == 50
        adjustment.hasInvoices
        adjustment.hasPrepaymentInvoice
        adjustment.hasRegularInvoice
        adjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.PARTIALLY_INVOICED
    }

    void 'without invoice items nothing is invoiced'() {
        given:
        OrderAdjustment adjustment = new OrderAdjustment(amount: 10, invoiceItems: [] as Set, order: placedOrder())

        expect:
        adjustment.invoicedUnitPrice == 0
        adjustment.unitPriceOnPostedInvoices == 0
        adjustment.inversedUnitPrice == 0
        !adjustment.hasInvoices
        !adjustment.hasPrepaymentInvoice
        adjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.NOT_INVOICED
        adjustment.canBeOnRegularInvoice()
        adjustment.isInvoiceable()
    }

    void 'a fully posted adjustment is invoiced and cannot be invoiced again'() {
        given:
        OrderAdjustment adjustment = new OrderAdjustment(amount: 10, order: placedOrder(),
                invoiceItems: [item(10, [posted: true])] as Set)

        expect:
        adjustment.isFullyInvoiced
        adjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.INVOICED
        !adjustment.isInvoiceable()
        adjustment.unitPriceAvailableToInvoice == 0
    }

    void 'canceled adjustments are invoiceable only to reverse a prepayment'() {
        given:
        OrderAdjustment prepaid = new OrderAdjustment(amount: 10, canceled: true, order: placedOrder(),
                invoiceItems: [item(10, [type: prepayment])] as Set)
        OrderAdjustment notPrepaid = new OrderAdjustment(amount: 10, canceled: true, order: placedOrder(),
                invoiceItems: [] as Set)

        expect:
        prepaid.canBeOnRegularInvoice()
        prepaid.isInvoiceable()
        prepaid.unitPriceAvailableToInvoice == 0
        !notPrepaid.canBeOnRegularInvoice()
        !notPrepaid.isInvoiceable()
    }

    void 'a zero adjustment is invoiceable once on a placed order'() {
        given:
        OrderAdjustment fresh = new OrderAdjustment(amount: 0, order: placedOrder(), invoiceItems: [] as Set)
        OrderAdjustment invoiced = new OrderAdjustment(amount: 0, order: placedOrder(), invoiceItems: [item(0)] as Set)
        OrderAdjustment pending = new OrderAdjustment(amount: 0, order: new Order(status: OrderStatus.PENDING),
                invoiceItems: [] as Set)

        expect:
        fresh.isInvoiceable()
        !invoiced.isInvoiceable()
        !pending.isInvoiceable()
    }

    void 'adjustments sort by creation date, then last update, then id'() {
        given:
        Date earlier = new Date(1000)
        Date later = new Date(2000)
        OrderAdjustment a = adjustment(earlier, later)
        OrderAdjustment b = adjustment(later, earlier)
        OrderAdjustment c = adjustment(earlier, earlier)
        OrderAdjustment d = adjustment(earlier, earlier)
        c.id = 'c'
        d.id = 'd'

        expect:
        [b, a, d, c].sort() == [c, d, a, b]
    }
}
