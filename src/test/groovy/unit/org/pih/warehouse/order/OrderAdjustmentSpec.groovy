package unit.org.pih.warehouse.order

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.OrderAdjustmentInvoiceStatus
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class OrderAdjustmentSpec extends Specification implements DomainUnitTest<OrderAdjustment> {
    void 'OrderAdjustment.getIsInvoiceable() should return #isInvoiceable when canceled: #canceled, the status of order is: #orderStatus and has regular invoice: #hasRegularInvoice'() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getHasRegularInvoice() >> hasRegularInvoice
            getHasPrepaymentInvoice() >> true
            getTotalAdjustments() >> 4
            getInvoicedUnitPrice() >> 3
        }
        orderAdjustment.canceled = canceled
        orderAdjustment.order = Spy(Order)
        orderAdjustment.order.status = orderStatus

        expect:
        orderAdjustment.invoiceable == isInvoiceable

        where:
        orderStatus         | hasRegularInvoice  |  canceled || isInvoiceable
        OrderStatus.PENDING | false              |  true     || false
        OrderStatus.PLACED  | false              |  true     || true
        OrderStatus.PENDING | false              |  false    || false
        OrderStatus.PLACED  | true               |  true     || false
        OrderStatus.PLACED  | true               |  false    || true
    }


    void 'OrderAdjustment.getIsInvoiceable() should return #isInvoiceable when trying to invoice #unitPriceInvoiced out of 4 adjustments'() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getHasRegularInvoice() >> hasRegularInvoice
            getHasPrepaymentInvoice() >> true
            getTotalAdjustments() >> 4
            getInvoicedUnitPrice() >> unitPriceInvoiced
        }
        orderAdjustment.canceled = canceled
        orderAdjustment.order = Spy(Order)
        orderAdjustment.order.status = OrderStatus.PLACED

        expect:
        orderAdjustment.invoiceable == isInvoiceable

        where:
        hasRegularInvoice  | canceled   | unitPriceInvoiced || isInvoiceable
        false              | true       | 3                 || true
        false              | true       | 4                 || false
        false              | false      | 3                 || true
        false              | false      | 4                 || false
        true               | false      | 3                 || true
        true               | false      | 4                 || false
    }

    void "derivedPaymentStatus should return NOT_INVOICED when postedPurchaseInvoiceItems is empty"() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getPostedPurchaseInvoiceItems() >> []
            getTotalAdjustments() >> 100
            getUnitPriceOnPostedInvoices() >> 100
        }
        orderAdjustment.canceled = false

        expect:
        orderAdjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.NOT_INVOICED
    }

    void "derivedPaymentStatus should return INVOICED when order is canceled or totalAdjustments is zero"() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getPostedPurchaseInvoiceItems() >> [
                    Mock(InvoiceItem)
            ]
            getTotalAdjustments() >> totalAdjustments
            getUnitPriceOnPostedInvoices() >> 100
        }
        orderAdjustment.canceled = canceled

        expect: "The derived payment status is INVOICED"
        orderAdjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.INVOICED

        where:
        totalAdjustments | canceled
        0                | false
        100              | true
    }

    void "derivedPaymentStatus should return NOT_INVOICED when unitPriceOnPostedInvoices is zero"() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getPostedPurchaseInvoiceItems() >> [
                    Mock(InvoiceItem)
            ]
            getTotalAdjustments() >> 100
            getUnitPriceOnPostedInvoices() >> 0
        }
        orderAdjustment.canceled = false

        expect:
        orderAdjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.NOT_INVOICED
    }

    void "derivedPaymentStatus should return INVOICED when fully invoiced"() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getPostedPurchaseInvoiceItems() >> [
                    Mock(InvoiceItem)
            ]
            getTotalAdjustments() >> 100
            getUnitPriceOnPostedInvoices() >> 100
        }
        orderAdjustment.canceled = false

        expect:
        orderAdjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.INVOICED
    }

    void "derivedPaymentStatus should return PARTIALLY_INVOICED when none of the other conditions apply"() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getPostedPurchaseInvoiceItems() >> [
                    Mock(InvoiceItem)
            ]
            getTotalAdjustments() >> 100
            getUnitPriceOnPostedInvoices() >> 50
        }
        orderAdjustment.canceled = false

        expect:
        orderAdjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.PARTIALLY_INVOICED
    }

    void "derivedPaymentStatus should return INVOICED when invoiced more than total adjustment"() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getPostedPurchaseInvoiceItems() >> [
                    Mock(InvoiceItem)
            ]
            getTotalAdjustments() >> 100
            getUnitPriceOnPostedInvoices() >> 120
        }
        orderAdjustment.canceled = false

        expect:
        orderAdjustment.derivedPaymentStatus == OrderAdjustmentInvoiceStatus.INVOICED
    }

}
