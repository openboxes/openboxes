package unit.org.pih.warehouse.order

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import spock.lang.Specification

class OrderSpec extends Specification implements DomainUnitTest<Order> {
    void "Order.getCanGenerateInvoice() should return true when there is an order adjustment that hasn't already been invoiced"() {
        given:
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }
        Set<OrderItem> orderItems = [
                Mock(OrderItem),
                Stub(OrderItem) {
                    getHasNotInvoicedAdjustment() >> hasNotInvoicedAdjustment
                },
                Mock(OrderItem)
        ]
        order.setOrderItems(orderItems)

        expect:
        order.canGenerateInvoice == canGenerateInvoice

        where:
        hasPrepaymentInvoice | hasNotInvoicedAdjustment || canGenerateInvoice
        false                | true                     || false
        true                 | false                    || false
        true                 | true                     || true
    }

    void "Order.getCanGenerateInvoice() should return true when there is not invoiced order item"() {
        given:
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }
        OrderItem orderItem = Spy(OrderItem) {
            getIsEncumbered() >> isEncumbered
            getAllInvoiceItems() >> []
        }
        orderItem.orderItemStatusCode = orderItemStatusCode
        Set<OrderItem> orderItems = [
                Mock(OrderItem),
                orderItem,
                Mock(OrderItem)
        ]
        order.setOrderItems(orderItems)

        expect:
        order.canGenerateInvoice == canGenerateInvoice

        where:
        hasPrepaymentInvoice | orderItemStatusCode          | isEncumbered || canGenerateInvoice
        false                | OrderItemStatusCode.CANCELED | false        || false
        true                 | OrderItemStatusCode.CANCELED | false        || false
        true                 | OrderItemStatusCode.PENDING  | true         || false
        true                 | OrderItemStatusCode.CANCELED | true         || true
    }

    void "Order.getCanGenerateInvoice() should return true when when not all of the quantity has been invoiced"() {
        given:
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }
        OrderItem orderItem = Spy(OrderItem) {
            getIsEncumbered() >> isEncumbered
            getIsInvoiceable() >> isInvoiceable
        }
        Set<OrderItem> orderItems = [
                Mock(OrderItem),
                orderItem,
                Mock(OrderItem)
        ]
        order.setOrderItems(orderItems)

        expect:
            order.canGenerateInvoice == canGenerateInvoice

        where:
            hasPrepaymentInvoice | isEncumbered | isInvoiceable || canGenerateInvoice
            false                | true         | true          || false
            true                 | false        | true          || false
            true                 | true         | false         || false
            true                 | true         | true          || true
    }
}
