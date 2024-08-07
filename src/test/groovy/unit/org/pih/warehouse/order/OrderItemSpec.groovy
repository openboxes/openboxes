package unit.org.pih.warehouse.order

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.order.OrderItem
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class OrderItemSpec extends Specification implements DomainUnitTest<OrderItem> {
    void 'OrderItem.isCompletelyInvoiced() should return: #isCompletelyInvoiced when quantity is: #quantity and quantityInvoicedInStandardUom is: #quantityInvoicedInStandardUom'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getPostedQuantityInvoicedInStandardUom() >> quantityInvoicedInStandardUom
        }
        orderItem.quantity = quantity

        expect:
        orderItem.completelyInvoiced == isCompletelyInvoiced

        where:
        quantityInvoicedInStandardUom | quantity || isCompletelyInvoiced
        2                             | 1        || true
        1                             | 1        || true
        0                             | 1        || false
    }

    void 'OrderItem.getQuantityAvailableToInvoice() should return: #quantity when quantityInvoiced: #quantityInvoiced and getQuantityShipped: #quantityShipped and canceled: #canceled'() {
        given:
            OrderItem orderItem = Spy(OrderItem) {
                isCanceled() >> canceled
                getQuantityInvoicedInStandardUom() >> quantityInvoiced
                getQuantityShippedInStandardUom() >> quantityShipped
            }

        expect:
            orderItem.quantityAvailableToInvoice == quantity

        where:
           quantityShipped | quantityInvoiced | canceled || quantity
           1               | 1                | true     || null
           2               | 1                | false    || 1
    }

    void 'OrderItem.isInvoiceable() should return: #isInoviceable when quantity available to invoice is #quantityAvailableToInvoice'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getHasPrepaymentInvoice() >> true
            isFullyInvoiced() >> false
            getQuantityAvailableToInvoice() >> quantityAvailableToInvoice
        }

        expect:
        orderItem.invoiceable == isInvoiceable

        where:
        quantityAvailableToInvoice || isInvoiceable
        1                          || true
        0                          || false
        -10                        || false
        100                        || true
    }

    void 'OrderItem.isFullyInvoiced() should return: #isFullyInvoiced when quantityInvoicedInStandardUom: #quantityInvoicedInStandardUom and quantity: #quantity'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getQuantityInvoiced() >> quantityInvoiced
        }
        orderItem.quantity = quantity

        expect:
        orderItem.isFullyInvoiced() == isFullyInvoiced

        where:
        quantityInvoiced | quantity || isFullyInvoiced
        2                | 1        || true
        1                | 1        || true
        0                | 1        || false
    }
}
