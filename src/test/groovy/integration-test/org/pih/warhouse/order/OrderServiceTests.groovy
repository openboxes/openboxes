package org.pih.warehouse.order

import grails.validation.ValidationException
import grails.util.Holders
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import testutils.DbHelper

@Ignore
class OrderServiceTests extends GroovyTestCase {

	def orderService

	protected void setUp() {
		super.setUp()
		def locationType = DbHelper.findOrCreateLocationType('Depot')
		DbHelper.findOrCreateLocation('Origin', locationType)
		DbHelper.findOrCreateLocation('Destination', locationType)
		DbHelper.findOrCreateAdminUser('Justin', 'Miranda', 'justin.miranda@gmail.com', 'justin.miranda', 'password', true)

		Holders.config.openboxes.identifier.purchaseOrder.generatorType = IdentifierGeneratorTypeCode.RANDOM
	}

	@Test
	void saveOrder_shouldThrowValidationException() {
		shouldFail(ValidationException) {
			def newOrder = new Order()
			orderService.saveOrder(newOrder)
		} 
	}

	@Test
	void saveOrder_shouldSaveSuccessfully() {
		def newOrder = new Order();
		newOrder.name = "Order 1234"
		newOrder.orderType = OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		orderService.saveOrder(newOrder)
		assertNotNull newOrder.orderNumber
	}

	@Test
	void saveOrder_shouldGenerateOrderNumber() { 		
		def newOrder = new Order();
		newOrder.name = "Order 1234"
		newOrder.orderType = OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		orderService.saveOrder(newOrder)
		assertNotNull newOrder.orderNumber
	}

	@Test
	void saveOrder_shouldNotGenerateOrderNumber() {
		def newOrder = new Order();
		newOrder.name = "Order 1234"
		newOrder.orderNumber = "PO-12345"
		newOrder.orderType = OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		orderService.saveOrder(newOrder)
		assertEquals "PO-12345", newOrder.orderNumber
	}
}
