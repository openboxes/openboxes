package unit.org.pih.warehouse.invoice

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceService
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class InvoiceServiceSpec extends Specification implements ServiceUnitTest<InvoiceService>, DataTest {

    @Override
    Class<?>[] getDomainClassesToMock(){
        return [ShipmentItem, InvoiceItem] as Class[]
    }

    void 'InvoiceService.createFromShipmentItem() should return quantity to invoice: #quantityToInvoice when quantity is: #quantity and quantity invoiced: #quantityInvoiced'() {
        given:
        ShipmentItem shipmentItem = Spy(ShipmentItem) {
            getQuantityInvoiced() >> quantityInvoiced
        }
        shipmentItem.quantity = quantity
        shipmentItem.product = Stub(Product)
        shipmentItem.product.glAccount = Mock(GlAccount)
        shipmentItem.addToOrderItems(new OrderItem())

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
