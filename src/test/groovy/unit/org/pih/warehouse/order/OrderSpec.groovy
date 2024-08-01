package unit.org.pih.warehouse.order

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class OrderSpec extends Specification implements DomainUnitTest<Order> {
    void "Order.getCanGenerateInvoice() should return true when there is an order adjustment not tied to any order item that hasn't already been invoiced"() {
        given:
        OrderAdjustment orderAdjustment = Stub(OrderAdjustment) {
            isInvoiceable() >> true
        }
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }

        order.addToOrderAdjustments(orderAdjustment)

        expect:
        order.canGenerateInvoice == canGenerateInvoice

        where:
        hasPrepaymentInvoice || canGenerateInvoice
        false                || false
        true                 || true
    }

    void "Order.getCanGenerateInvoice() should return true when there is an order adjustment that hasn't already been invoiced"() {
        given:
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }
        OrderItem orderItem = Stub(OrderItem)
        OrderAdjustment orderAdjustment = Stub(OrderAdjustment)
        orderAdjustment.isInvoiceable() >> isAdjustmentInvoiceable
        orderItem.addToOrderAdjustments(orderAdjustment)
        order.addToOrderAdjustments(orderAdjustment)
        Set<OrderItem> orderItems = [
                Mock(OrderItem),
                orderItem,
                Mock(OrderItem)
        ]
        order.setOrderItems(orderItems)

        expect:
        order.canGenerateInvoice == canGenerateInvoice

        where:
        hasPrepaymentInvoice | isAdjustmentInvoiceable  || canGenerateInvoice
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
            getAllInvoiceItems() >> []
        }
        orderItem.isEncumbered() >> isEncumbered
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
            getQuantityAvailableToInvoice() >> quantityAvailableToInvoice
        }
        orderItem.isEncumbered() >> isEncumbered
        Set<OrderItem> orderItems = [
                Mock(OrderItem),
                orderItem,
                Mock(OrderItem)
        ]
        order.setOrderItems(orderItems)

        expect:
            order.canGenerateInvoice == canGenerateInvoice

        where:
            hasPrepaymentInvoice | isEncumbered | quantityAvailableToInvoice || canGenerateInvoice
            false                | true         | 1                          || false
            true                 | false        | 1                          || false
            true                 | true         | 0                          || false
            true                 | true         | 1                          || true
    }

    void "Order.isFullyInvoiceable() should return #expected when item1 is #item1 and item2 is #item2"() {
        given:
        Order order = new Order(orderItems: [item1, item2])

        expect:
        order.isFullyInvoiceable() == expected

        where:
        item1                       | item2                         || expected
        mockOrderItem(true, true)   | mockOrderItem(true, true)     || true
        mockOrderItem(true, false)  | mockOrderItem(true, false)    || false
        mockOrderItem(false, true)  | mockOrderItem(true, true)     || true
        mockOrderItem(true, false)  | mockOrderItem(true, true)     || false
        mockOrderItem(false, true)  | mockOrderItem(false, true)    || false
        mockOrderItem(false, false) | mockOrderItem(false, false)   || false
    }

    private OrderItem mockOrderItem(boolean encumbered, boolean invoiceable) {
        return Mock(OrderItem) {
            isEncumbered() >> encumbered
            isInvoiceable() >> invoiceable
            toString() >> "encumbered: $encumbered, invoiceable: $invoiceable"
        }
    }
}
