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
            getQuantityInvoicedInStandardUom() >> quantityInvoicedInStandardUom
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
                getInvoicedQuantity() >> quantityInvoiced
                getQuantityShippedInStandardUom() >> quantityShipped
            }

        expect:
            orderItem.quantityAvailableToInvoice == quantity

        where:
           quantityShipped | quantityInvoiced | canceled || quantity
           1               | 1                | true     || null
           2               | 1                | false    || 1
    }

    void 'OrderItem.isInvoiceable() should return: #isInoviceable when quantity available to invoice is #quantityAvailableToInvoice and hasRegularInvoice: #hasRegularInvoice'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getHasPrepaymentInvoice() >> true
            getHasRegularInvoice() >> hasRegularInvoice
            getQuantityAvailableToInvoice() >> quantityAvailableToInvoice
            isEncumbered() >> true
        }

        expect:
        orderItem.invoiceable == isInvoiceable

        where:
        quantityAvailableToInvoice | hasRegularInvoice || isInvoiceable
        1                          | false             || true
        0                          | false             || false
        -10                        | false             || false
        100                        | false             || true
        1                          | true              || false
    }

    void 'OrderItem.isEncumbered() should return: #isEncumbered when quantityInvoicedInStandardUom: #quantityInvoicedInStandardUom and quantity: #quantity'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getQuantityInvoicedInStandardUom() >> quantityInvoicedInStandardUom
        }
        orderItem.quantity = quantity

        expect:
        orderItem.encumbered == isEncumbered

        where:
        quantityInvoicedInStandardUom | quantity || isEncumbered
        2                             | 1        || false
        1                             | 1        || false
        0                             | 1        || true
    }
}
