package unit.org.pih.warehouse.invoice

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceService
import org.pih.warehouse.invoice.PrepaymentInvoiceService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.shipping.ShipmentItem
import spock.lang.FailsWith
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class PrepaymentInvoiceServiceSpec extends Specification implements ServiceUnitTest<PrepaymentInvoiceService>, DataTest {

    @Shared
    InvoiceService invoiceService

    @Shared
    IdentifierService identifierService

    void setup() {
        mockDomain(InvoiceItem)
        invoiceService = Stub(InvoiceService) {
            createFromOrder(_ as Order) >> new Invoice()
        }

        identifierService = Stub(IdentifierService) {
            generateInvoiceIdentifier() >> UUID.randomUUID().toString()
        }
        invoiceService.identifierService = identifierService
        service.invoiceService = invoiceService
    }

    @FailsWith(Exception)
    void 'PrepaymentInvoiceService.generateInvoice() should not create invoice when prepayment invoice is missing'() {
        given:
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> false
        }

        expect:
        null != service.generateInvoice(order)
    }

    void 'PrepaymentInvoiceService.generateInvoice() should create invoice not including already invoiced items'() {
        given:
        // Already invoiced canceled item shouldn't be invoiced again
        OrderItem canceledItem = Stub(OrderItem) {
            isInvoiceable() >> false
        }
        canceledItem.orderItemStatusCode = OrderItemStatusCode.CANCELED
        // Already invoiced item without qty shouldn't be invoiced again
        OrderItem notCanceledItem = Stub(OrderItem) {
            isInvoiceable() >> false
        }
        // Already invoiced adjustment shouldn't be invoiced again
        OrderAdjustment alreadyInvoicedAdjustment = Stub(OrderAdjustment) {
            isInvoiceable() >> false
        }
        // Invoiceable order item, should be visible on invoice
        OrderItem invoiceableOrderItem = Stub(OrderItem) {
            isInvoiceable() >> true
            getInvoiceableShipmentItems() >> [Mock(ShipmentItem)]
        }
        // Invoiceable order adjustment, should be visible on invoice
        OrderAdjustment invoiceableOrderAdjustment = Stub(OrderAdjustment) {
            isInvoiceable() >> true
        }

        and:
        Set<OrderAdjustment> orderAdjustments = [
                alreadyInvoicedAdjustment,
                invoiceableOrderAdjustment
        ]
        Set<OrderItem> orderItems = [
                notCanceledItem,
                canceledItem,
                invoiceableOrderItem,
        ]
        Order order = Spy(Order) {
            getHasPrepaymentInvoice() >> true
        }
        order.orderItems = orderItems
        order.orderAdjustments = orderAdjustments

        when:
        Invoice generatedInvoice = service.generateInvoice(order)

        then:
        generatedInvoice != null
        generatedInvoice.invoiceItems.size() == 2
    }
}
