package unit.org.pih.warehouse.order

import grails.testing.gorm.DomainUnitTest
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
            getInvoicedQuantity() >> 0
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
}
