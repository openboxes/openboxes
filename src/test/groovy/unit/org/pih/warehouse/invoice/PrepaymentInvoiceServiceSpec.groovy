package unit.org.pih.warehouse.invoice

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceService
import org.pih.warehouse.invoice.PrepaymentInvoiceService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Product
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

    void 'PrepaymentInvoiceService.createFromShipmentItem() should calculate quantity to invoice: #quantityToInvoice when quantity is: #quantity and quantity invoiced: #quantityInvoiced'() {
        given:
        mockDomain(ShipmentItem)
        mockDomain(InvoiceItem)
        ShipmentItem shipmentItem = Spy(ShipmentItem) {
            getQuantityInvoiced() >> quantityInvoiced
        }
        shipmentItem.quantity = quantity
        shipmentItem.product = Stub(Product)
        shipmentItem.product.glAccount = Mock(GlAccount)
        OrderItem orderItem = Spy(OrderItem)
        orderItem.unitPrice = 1
        shipmentItem.addToOrderItems(orderItem)

        when:
        InvoiceItem invoiceItem = service.createFromShipmentItem(shipmentItem)

        then:
        invoiceItem.quantity == quantityToInvoice

        where:
        quantity | quantityInvoiced || quantityToInvoice
        1        | 1                || 0
        5        | 2                || 3
        6        | 0                || 6
        0        | 0                || 0
    }
}
