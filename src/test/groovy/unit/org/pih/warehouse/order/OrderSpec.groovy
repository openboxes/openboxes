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
            canBeOnRegularInvoice() >> true
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
        orderAdjustment.canBeOnRegularInvoice() >> true
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
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
            getHasRegularInvoice() >> false
            isFullyInvoiced() >> false
            getQuantityInvoiced() >> 0
            getQuantityAvailableToInvoice() >> 1
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
        hasPrepaymentInvoice | orderItemStatusCode          || canGenerateInvoice
        false                | OrderItemStatusCode.CANCELED || false
        false                | OrderItemStatusCode.PENDING  || false
        true                 | OrderItemStatusCode.CANCELED || true
        true                 | OrderItemStatusCode.PENDING  || true
    }

    void "Order.getCanGenerateInvoice() should return true when when not all of the quantity has been invoiced"() {
        given:
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }
        OrderItem orderItem = Spy(OrderItem) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
            getQuantityAvailableToInvoice() >> quantityAvailableToInvoice
        }
        orderItem.isFullyInvoiced() >> isFullyInvoiced
        Set<OrderItem> orderItems = [
                Mock(OrderItem),
                orderItem,
                Mock(OrderItem)
        ]
        order.setOrderItems(orderItems)

        expect:
            order.canGenerateInvoice == canGenerateInvoice

        where:
            hasPrepaymentInvoice | isFullyInvoiced | quantityAvailableToInvoice || canGenerateInvoice
            false                | false           | 1                          || false
            true                 | true            | 0                          || false
            true                 | false           | 0                          || false
            true                 | false           | 1                          || true
    }

    void "Order.isFullyInvoiceable() should return FALSE when there are no order items and no order adjustments"() {
        given: "An order with no order adjustments and no order items"
        Order order = new Order(orderItems: [], orderAdjustments: [])

        expect:
        order.isFullyInvoiceable() == false
    }

    void "Order.isFullyInvoiceable() should return TRUE when there are no order adjustments with all order items fully invoiceable"() {
        given: "An order with no order adjustments and all order items fully invoiceable"
        OrderItem item1 = Stub(OrderItem) {
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderItem item2 = Stub(OrderItem) {
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        Order order = new Order(orderItems: [item1, item2], orderAdjustments: [])

        expect:
        order.isFullyInvoiceable() == true
    }

    void "Order.isFullyInvoiceable() should return TRUE when there are no order items with all order adjustments fully invoiceable"() {
        given: "An order with no order items and all order adjustments fully invoiceable"
        OrderAdjustment item1 = Stub(OrderAdjustment) {
            isInvoiceable() >> true
        }
        OrderAdjustment item2 = Stub(OrderAdjustment) {
            isInvoiceable() >> true
        }
        Order order = new Order(orderItems: [], orderAdjustments: [item1, item2])

        expect:
        order.isFullyInvoiceable() == true
    }

    void "Order.isFullyInvoiceable() should return FALSE when all order items are invoiceable and all adjustments are not"() {
        given: "An order with fully invoicebale order items and all order adjustments not invoiceable"
        OrderItem orderItem1 = Stub(OrderItem) {
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderItem orderItem2 = Stub(OrderItem) {
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderAdjustment adjustmentItem1 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> false
        }
        OrderAdjustment adjustmentItem2 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> false
        }
        Order order = new Order(orderItems: [orderItem1, orderItem2], orderAdjustments: [adjustmentItem1, adjustmentItem2])

        expect:
        order.isFullyInvoiceable() == false
    }

    void "Order.isFullyInvoiceable() should return FALSE when all order items are invoiceable and one adjustment are not"() {
        given: "An order with fully invoicebale order items and one order adjustment not invoiceable"
        OrderItem orderItem1 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderItem orderItem2 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderAdjustment adjustmentItem1 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
        }
        OrderAdjustment adjustmentItem2 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> false
        }
        Order order = new Order(orderItems: [orderItem1, orderItem2], orderAdjustments: [adjustmentItem1, adjustmentItem2])

        expect:
        order.isFullyInvoiceable() == false
    }

    void "Order.isFullyInvoiceable() should return FALSE when all order adjustments are invoiceable and all order items are not"() {
        given: "An order with fully invoicebale order adjustments and all order items not invoiceable"
        OrderItem orderItem1 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> false
            getQuantityRemaining() >> 0
        }
        OrderItem orderItem2 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> false
            getQuantityRemaining() >> 0
        }
        OrderAdjustment adjustmentItem1 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
        }
        OrderAdjustment adjustmentItem2 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
        }
        Order order = new Order(orderItems: [orderItem1, orderItem2], orderAdjustments: [adjustmentItem1, adjustmentItem2])

        expect:
        order.isFullyInvoiceable() == false
    }

    void "Order.isFullyInvoiceable() should return FALSE when all order adjustments are invoiceable and one order item is not"() {
        given: "An order with fully invoicebale order adjustments and one order item not invoiceable"
        OrderItem orderItem1 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> false
            getQuantityRemaining() >> 0
        }
        OrderItem orderItem2 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderAdjustment adjustmentItem1 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
        }
        OrderAdjustment adjustmentItem2 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
        }
        Order order = new Order(orderItems: [orderItem1, orderItem2], orderAdjustments: [adjustmentItem1, adjustmentItem2])

        expect:
        order.isFullyInvoiceable() == false
    }

    void "Order.isFullyInvoiceable() should return FALSE when at least one order item has quantity remaining is greater than 0"() {
        given: "An order with fully invoiceable items and one order items with quantity remaining more than 0"
        OrderItem orderItem1 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
            getQuantityRemaining() >> 1
        }
        OrderItem orderItem2 = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderAdjustment adjustmentItem1 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
        }
        OrderAdjustment adjustmentItem2 = Stub(OrderAdjustment) {
            canBeOnRegularInvoice() >> true
            isInvoiceable() >> true
        }
        Order order = new Order(orderItems: [orderItem1, orderItem2], orderAdjustments: [adjustmentItem1, adjustmentItem2])

        expect:
        order.isFullyInvoiceable() == false
    }
}
