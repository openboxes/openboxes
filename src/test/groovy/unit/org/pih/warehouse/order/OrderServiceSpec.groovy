package unit.org.pih.warehouse.order

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import spock.lang.Specification

import org.pih.warehouse.order.OrderService
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.order.PurchaseOrderIdentifierService

class OrderServiceSpec extends Specification implements DataTest, ServiceUnitTest<OrderService> {

    void setupSpec() {
        mockDomains(Order)
    }

    void 'saveOrder should succeed for a valid order'() {
        given:
        service.purchaseOrderIdentifierService = Stub(PurchaseOrderIdentifierService) {
            generate(_ as Order) >> "TEST-ID"
        }

        and:
        LocationType pharmacyType = new LocationType(locationTypeCode: LocationTypeCode.DISPENSARY)
        Organization originOrg = new Organization()
        Order order = new Order(
                name: "Order 1234",
                orderType: new OrderType(code: OrderTypeCode.PURCHASE_ORDER.name()),
                origin: new Location(name: "Origin", locationType: pharmacyType, organization: originOrg),
                destination: new Location(name: "Destination", locationType: pharmacyType),
                orderedBy: new User(username: "justin.miranda"),
        )

        when:
        service.saveOrder(order)

        then:
        assert order.orderNumber == "TEST-ID"
        assert order.originParty == originOrg
    }

    void 'saveOrder should not regenerate order number if one already exists for the order'() {
        given: 'an order that already has an order number'
        LocationType pharmacyType = new LocationType(locationTypeCode: LocationTypeCode.DISPENSARY)
        Order order = new Order(
                name: "Order 1234",
                orderNumber: "EXISTING-ID",
                orderType: new OrderType(code: OrderTypeCode.PURCHASE_ORDER.name()),
                origin: new Location(name: "Origin", locationType: pharmacyType),
                destination: new Location(name: "Destination", locationType: pharmacyType),
                orderedBy: new User(username: "justin.miranda"),
        )

        when:
        service.saveOrder(order)

        then: 'the order number should remain unchanged'
        assert order.orderNumber == "EXISTING-ID"
    }
}
