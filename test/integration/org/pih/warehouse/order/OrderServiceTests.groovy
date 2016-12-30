package org.pih.warehouse.order

import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import org.junit.Before
import org.junit.Test
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.User;

import testutils.DbHelper;
import static org.junit.Assert.*


@Integration
class OrderServiceTests {

	def orderService

	@Before
	void setUp() {
		def locationType = new LocationType(name: "Depot").save(flush:true)
		new Location(name: "Origin", locationType: locationType).save(flush:true)
		new Location(name: "Destination", locationType: locationType).save(flush:true)
		DbHelper.createAdmin("Justin", "Miranda", "justin.miranda@gmail.com", "justin.miranda", "password", true)
	}

	@Test
	void test_saveOrder_shouldThrowOrderException() {
		shouldFail(OrderException) {
			def newOrder = new Order();
			orderService.saveOrder(newOrder);
		} 
	}

	@Test
	void test_saveOrder_shouldSaveSuccessfully() {
		def newOrder = new Order();
		newOrder.description = "Order 1234"
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")			
		orderService.saveOrder(newOrder);
		assertNotNull newOrder.orderNumber
	}

	@Test
	void test_saveOrder_shouldGenerateOrderNumber() {
		def newOrder = new Order();
		newOrder.description = "Order 1234"
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		orderService.saveOrder(newOrder);
		assertNotNull newOrder.orderNumber
	}

	@Test
	void test_saveOrder_shouldNotGenerateOrderNumber() {
		def newOrder = new Order();
		newOrder.orderNumber = "PO-12345"
		newOrder.description = "Order 1234"
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		orderService.saveOrder(newOrder);
		assertEquals "PO-12345", newOrder.orderNumber
	}

	
		
}
