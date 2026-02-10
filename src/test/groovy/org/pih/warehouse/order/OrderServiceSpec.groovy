package org.pih.warehouse.order

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.core.session.SessionManager
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import spock.lang.Specification

class OrderServiceSpec extends Specification implements DataTest, ServiceUnitTest<OrderService> {

    void setup() {
        GroovyMock(CSVUtils, global: true)

        CSVUtils.parseNumber(_, _) >> { String s, String field ->
            return s ? new BigDecimal(s.trim()) : null
        }

        CSVUtils.parseInteger(_, _) >> { String s, String field ->
            return s ? Integer.parseInt(s.trim()) : 0
        }
    }

    void setupSpec() {
        mockDomains(Order, OrderItem, Product, ProductSupplier, Organization, Location, User, UnitOfMeasure, BudgetCode, Person)
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

    void 'ImportOrderItems should fail if creating new item with inactive source'() {
        given: 'current location and user'
        Location currentLocation = new Location(id: "1", name: "Location", organization: new Organization())
        User user = new User(username: "anadolny")

        and: 'existing order'
        Order order = new Order(orderNumber: "ORDER-NUMBER", status: OrderStatus.PENDING)
        order.save(validate: false)

        and: 'product and inactive supplier'
        Product product = new Product(
                productCode: "11058",
                name: "Test strip, Urine, 10 analytes per strip, 1 test"
        ).save(validate: false)
        ProductSupplier inactiveSupplier = new ProductSupplier(
                code: "INACTIVE-SUPPLIER",
                product: product,
                active: false
        ).save(validate: false)

        and: 'valid UOM and BudgetCode'
        new UnitOfMeasure(code: "EA", name: "Each").save(validate: false)
        new BudgetCode(code: "BC", active: true).save(validate: false)

        and: 'import data for a new item (no ID) using inactive source'
        List<Map<String, String>> importItems = [
                [
                   id: "",
                   productCode: "11058",
                   sourceCode: "INACTIVE-SUPPLIER",
                   quantity: "10",
                   unitOfMeasure: "EA/1",
                   unitPrice: "5",
                   budgetCode: "BC"
               ]
        ]

        when:
        service.importOrderItems(order.id, inactiveSupplier.id, importItems, currentLocation, user)

        then:
        // The service catches ProductException and rethrows as RuntimeException,
        // so we check the message of exception to confirm it's the expected error about inactive supplier.
        RuntimeException e = thrown(RuntimeException)
        e.message.contains("Product source INACTIVE-SUPPLIER for product 11058 is inactive")
    }

    void 'ImportOrderItems should fail if changing product code for existing item'() {
        given: 'current location and user'
        Location currentLocation = new Location(id: "1", name: "Location", organization: new Organization())
        User user = new User(username: "anadolny")

        and: 'two products'
        Product product11058 =  new Product(
                productCode: "11058",
                name: "Test strip, Urine, 10 analytes per strip, 1 test"
        ).save(validate: false)
        new Product(
                productCode: "10034",
                name: "Vitamin B Compound, tablet"
        ).save(validate: false)

        and: 'product supplier for product 11058'
        ProductSupplier productSupplier = new ProductSupplier(
                code: "ACTIVE-SUPPLIER",
                product: product11058,
                active: true
        ).save(validate: false)

        and: 'existing order with orderItem'
        Order order = new Order(orderNumber: "ORDER-NUMBER", status: OrderStatus.PENDING)
        OrderItem item = new OrderItem(
                product: product11058,
                productSupplier: productSupplier,
                quantity: 5
        ).save(validate: false)
        order.addToOrderItems(item)
        order.save(validate: false)

        and: 'valid UOM and BudgetCode'
        new UnitOfMeasure(code: "EA", name: "Each").save(validate: false)
        new BudgetCode(code: "BC", active: true).save(validate: false)

        and: 'import data trying to change product from 11058 to 10034'
        List<Map<String, String>> importItems = [
                [
                        id: item.id,
                        productCode: "10034",
                        sourceCode: "ACTIVE-SUPPLIER",
                        quantity: "10",
                        unitOfMeasure: "EA/1",
                        unitPrice: "5",
                        budgetCode: "BC"
                ]
        ]

        when:
        service.importOrderItems(order.id, productSupplier.id, importItems, currentLocation, user)

        then:
        // The service catches ProductException and rethrows as RuntimeException,
        // so we check the message of exception to confirm it's the expected error about changing product code.
        RuntimeException e = thrown(RuntimeException)
        e.message.contains("Cannot change the product for an existing order item via import")
    }

    void 'ImportOrderItems should fail if new quantity is less than quantity in shipments'() {
        given: 'current user, location, an order that is placed'
        Location currentLocation = new Location(id: "1", name: "Location", organization: new Organization())
        User user = new User(username: "anadolny")
        Order order = new Order(orderNumber: "ORDER-SHIPPED", status: OrderStatus.PLACED)

        and: 'mock userService to allow editing placed orders'
        service.userService = Stub(UserService) {
            hasRolePurchaseApprover(_) >> true
        }

        and: 'an order item'
        Product product = new Product(productCode: "10034").save(validate: false)
        OrderItem item = new OrderItem(product: product, quantity: 50)
        order.addToOrderItems(item)
        order.save(validate: false)

        and: 'product supplier for product'
        ProductSupplier productSupplier = new ProductSupplier(
                code: "ACTIVE-SUPPLIER",
                product: product,
                active: true
        ).save(validate: false)

        and: 'mock the quantityInShipments property to simulate items being shipped'
        OrderItem.metaClass.getQuantityInShipments = { -> return 50 }

        and: 'import data trying to reduce quantity to 40 (less than 50 shipped)'
        new UnitOfMeasure(code: "EA", name: "Each").save(validate: false)
        new BudgetCode(code: "BC-1", active: true).save(validate: false)

        List<Map<String, String>> importItems = [
            [
               id: item.id,
               productCode: "10034",
               sourceCode: "ACTIVE-SUPPLIER",
               quantity: "40",
               unitOfMeasure: "EA/1",
               unitPrice: "10.0",
               budgetCode: "BC-1"
           ]
        ]

        when:
        service.importOrderItems(order.id, productSupplier.id, importItems, currentLocation, user)

        then:
        RuntimeException e = thrown(RuntimeException)
        e.message.contains("Must enter a quantity greater than or equal to the quantity in shipments (50)")

        cleanup:
        OrderItem.metaClass.getQuantityInShipments = null
    }
}
