package org.pih.warehouse.regression.platform.importer

import spock.lang.Ignore

import org.pih.warehouse.importer.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.data.PersonService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier

/**
 * Person lookups go through a stubbed PersonService that only knows "Jane Doe"; the order item lookup used by the
 * import when no order item id is given goes through a mocked OrderService.
 */
@Unroll
class PurchaseOrderActualReadyDateImportDataServiceSpec extends Specification implements ServiceUnitTest<PurchaseOrderActualReadyDateImportDataService>, DataTest {

    static final Date READY_DATE = new GregorianCalendar(2026, Calendar.MARCH, 15).time
    static final Date OLD_READY_DATE = new GregorianCalendar(2026, Calendar.JANUARY, 2).time

    Product aspirin
    Product paracetamol
    Order order
    Order otherOrder
    OrderItem aspirinItem
    OrderItem otherOrderItem
    Person jane

    OrderService orderService = Mock()

    void setupSpec() {
        mockDomains(Order, OrderItem, Product, ProductSupplier, Organization, Location, User, UnitOfMeasure, Person)
    }

    void setup() {
        jane = new Person(firstName: 'Jane', lastName: 'Doe', active: true).save(validate: false)
        service.orderService = orderService
        service.personService = Mock(PersonService) {
            getActivePerson('Jane Doe') >> jane
        }

        aspirin = new Product(productCode: 'ASP', name: 'Aspirin').save(validate: false)
        paracetamol = new Product(productCode: 'PAR', name: 'Paracetamol').save(validate: false)
        order = new Order(orderNumber: 'PO-1', name: 'First').save(validate: false)
        otherOrder = new Order(orderNumber: 'PO-2', name: 'Second').save(validate: false)
        aspirinItem = saveItem(order, aspirin)
        otherOrderItem = saveItem(otherOrder, paracetamol)
    }

    void 'importData should set the ready date and recipient on the order item given by id'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [[
                orderItemId: aspirinItem.id, actualReadyDate: READY_DATE, recipient: 'Jane Doe']])

        when:
        service.importData(command)

        then:
        0 * orderService.getOrderItemByOrderAndProduct(*_)

        and:
        OrderItem item = OrderItem.get(aspirinItem.id)
        item.actualReadyDate == READY_DATE
        item.recipient == jane
    }

    void 'importData should look the order item up by order number and product code when no id is given'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [[orderNumber: 'PO-1', productCode: 'ASP', actualReadyDate: READY_DATE]])

        when:
        service.importData(command)

        then:
        1 * orderService.getOrderItemByOrderAndProduct('PO-1', 'ASP') >> aspirinItem

        and:
        OrderItem.get(aspirinItem.id).actualReadyDate == READY_DATE
    }

    void 'importData should fail when the order item cannot be found'() {
        given:
        ImportDataCommand command = new ImportDataCommand(data: [[orderItemId: 'missing', orderNumber: 'PO-1', productCode: 'PAR']])

        when:
        service.importData(command)

        then:
        1 * orderService.getOrderItemByOrderAndProduct('PO-1', 'PAR') >> null
        IllegalArgumentException e = thrown()
        e.message == 'Order Item is not found'
    }

    private static OrderItem saveItem(Order order, Product product) {
        OrderItem orderItem = new OrderItem(product: product, quantity: 10, quantityPerUom: 1)
        order.addToOrderItems(orderItem)
        order.save(validate: false)
        return orderItem.save(validate: false)
    }
}
