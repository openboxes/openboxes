package unit.org.pih.warehouse.order

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class OrderAdjustmentSpec extends Specification implements DomainUnitTest<OrderAdjustment> {
    void 'OrderAdjustment.getIsInvoiceable() should return #isInvoiceable when isInvoiced: #isInvoiced and the status of order is: #orderStatus'() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getIsInvoiced() >> isInvoiced
        }
        orderAdjustment.order = Spy(Order)
        orderAdjustment.order.status = orderStatus

        expect:
        orderAdjustment.invoiceable == isInvoiceable

        where:
        isInvoiced | orderStatus         || isInvoiceable
        true       | OrderStatus.PENDING || false
        false      | OrderStatus.PLACED  || true
        true       | OrderStatus.PLACED  || false
        false      | OrderStatus.PENDING || false
    }

    void 'OrderAdjustment.getIsEncumbered() should return: #isEncumbered when isInvoiced: #isInvoiced and hasPrepaymentInvoice: #hasPrepaymentInvoice'() {
        given:
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getIsInvoiced() >> isInvoiced
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }

        Order order = Spy(Order)
        order.status = OrderStatus.PLACED
        orderAdjustment.order = order

        expect:
        orderAdjustment.encumbered == isEncumbered

        where:
        isInvoiced | hasPrepaymentInvoice || isEncumbered
        true       | true                 || false
        true       | false                || false
        false      | true                 || true
        false      | false                || false
    }
}
