package org.pih.warehouse.order

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.User
import org.pih.warehouse.core.session.SessionManager
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
        given: 'a stubbed identifier to use'
        service.purchaseOrderIdentifierService = Stub(PurchaseOrderIdentifierService) {
            generate(_ as Order) >> "TEST-ID"
        }

        and: 'a valid location type'
        LocationType pharmacyType = new LocationType(locationTypeCode: LocationTypeCode.DISPENSARY)

        and: 'the currently logged in location and organization (which will be the destination)'
        Organization loggedInOrganization = new Organization()
        loggedInOrganization.id = '1'
        Location loggedInLocation = new Location(
                organization: loggedInOrganization,
                locationType: pharmacyType,
        )
        service.sessionManager = Stub(SessionManager) {
            getCurrentLocation() >> loggedInLocation
        }

        and: 'a valid origin location and organization'
        Organization originOrganization = new Organization()
        Location originLocation = new Location(
                organization: originOrganization,
                locationType: pharmacyType,
        )

        and:
        Order order = new Order(
                name: "Order 1234",
                orderType: new OrderType(code: OrderTypeCode.PURCHASE_ORDER.name()),
                origin: originLocation,
                destination: loggedInLocation,
                destinationParty: loggedInOrganization,
                orderedBy: new User(username: "justin.miranda"),
        )

        when:
        service.saveOrder(order)

        then:
        assert order.orderNumber == "TEST-ID"
        assert order.originParty == originOrganization
    }

    void 'OBPIH-7448: saveOrder should fail if the destination party differs from the logged in organization'() {
        given: 'a valid location type'
        LocationType pharmacyType = new LocationType(locationTypeCode: LocationTypeCode.DISPENSARY)

        and: 'the currently logged in location and organization'
        Organization loggedInOrganization = new Organization()
        loggedInOrganization.id = '1'
        Location loggedInLocation = new Location(
                organization: loggedInOrganization,
                locationType: pharmacyType,
        )
        service.sessionManager = Stub(SessionManager) {
            getCurrentLocation() >> loggedInLocation
        }

        and: 'a destination location and organization (that is not the logged in one)'
        Organization destinationOrganization = new Organization()
        Location destinationLocation = new Location(name: "Destination", locationType: pharmacyType)

        and: 'a valid origin location and organization'
        Organization originOrganization = new Organization()
        Location originLocation = new Location(
                organization: originOrganization,
                locationType: pharmacyType,
        )

        and:
        Order order = new Order(
                name: "Order 1234",
                orderType: new OrderType(code: OrderTypeCode.PURCHASE_ORDER.name()),
                origin: originLocation,
                destination: destinationLocation,
                destinationParty: destinationOrganization,
                orderedBy: new User(username: "justin.miranda"),
        )

        when:
        service.saveOrder(order)

        then:
        thrown(ValidationException)
    }

    void 'saveOrder should not regenerate order number if one already exists for the order'() {
        given: 'a valid location type'
        LocationType pharmacyType = new LocationType(locationTypeCode: LocationTypeCode.DISPENSARY)

        and: 'the currently logged in location and organization (which will be the destination)'
        Organization loggedInOrganization = new Organization()
        loggedInOrganization.id = '1'
        Location loggedInLocation = new Location(
                organization: loggedInOrganization,
                locationType: pharmacyType,
        )
        service.sessionManager = Stub(SessionManager) {
            getCurrentLocation() >> loggedInLocation
        }

        and: 'a valid origin location and organization'
        Organization originOrganization = new Organization()
        Location originLocation = new Location(
                organization: originOrganization,
                locationType: pharmacyType,
        )

        and: 'an order that already has an order number'
        Order order = new Order(
                name: "Order 1234",
                orderNumber: "EXISTING-ID",
                orderType: new OrderType(code: OrderTypeCode.PURCHASE_ORDER.name()),
                origin: originLocation,
                destination: loggedInLocation,
                destinationParty: loggedInOrganization,
                orderedBy: new User(username: "justin.miranda"),
        )

        when:
        service.saveOrder(order)

        then: 'the order number should remain unchanged'
        assert order.orderNumber == "EXISTING-ID"
    }
}
