package org.pih.warehouse.order

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.Transaction

class OrderServiceUnitTests extends GrailsUnitTestCase {
    def service
    def boston
    def miami
    def order1
    def order2

    void setUp() {
        super.setUp()

        def supplierType = new LocationType(id: Constants.SUPPLIER_LOCATION_TYPE_ID, name: "Supplier")
        mockDomain(LocationType, [supplierType])

        boston = new Location(id: "l1", name: "boston")
        miami = new Location(id: "l2", name: "miami", locationType: supplierType)
        mockDomain(Location, [boston, miami])

        order1 = new Order(destination: boston, name: "order1")
        order2 = new Order(origin: boston, name: "order2")
        mockDomain(Order, [order1, order2])

        mockDomain(Transaction)

        service = new OrderService()
    }

    void testGetIncomingOrders() {
        def incomingOrders = service.getIncomingOrders(boston)
        assertEquals(1, incomingOrders.size())
        assertEquals("order1", incomingOrders.get(0).name)
    }

    void testGetOutgoingOrders() {
        def outgoingOrders = service.getOutgoingOrders(boston)
        assertEquals(1, outgoingOrders.size())
        assertEquals("order2", outgoingOrders.get(0).name)
    }

    void testGetSuppliers() {
        def suppliers = service.getSuppliers()
        assertEquals(1, suppliers.size())
        assertEquals("miami", suppliers.get(0).name)
    }

    void testDeleteOrder() {
        service.deleteOrder(order1)
        def incomingOrders = service.getIncomingOrders(boston)
        assertEquals(0, incomingOrders.size())
    }
}
