package org.pih.warehouse.order

import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import grails.util.Holders
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import spock.lang.Specification
import testutils.DbHelper
import static org.junit.Assert.*;


//@Ignore
class OrderServiceTests extends Specification implements ServiceUnitTest<OrderService> {

	protected void setup() {
		def locationType = DbHelper.findOrCreateLocationType('Depot')
		DbHelper.findOrCreateLocation('Origin', locationType)
		DbHelper.findOrCreateLocation('Destination', locationType)
		DbHelper.findOrCreateAdminUser('Justin', 'Miranda', 'justin.miranda@gmail.com', 'justin.miranda', 'password', true)

		Holders.config.openboxes.identifier.purchaseOrder.generatorType = IdentifierGeneratorTypeCode.RANDOM
	}

	@Test
	void saveOrder_shouldThrowValidationException() {
		when:
		"testing new order"

		then:
		shouldFail(ValidationException) {
			def newOrder = new Order()
			service.saveOrder(newOrder)
		} 
	}

	@Test
	void saveOrder_shouldSaveSuccessfully() {
		when:
		def newOrder = new Order();
		newOrder.name = "Order 1234"
		newOrder.orderType = OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		service.saveOrder(newOrder)
		then:
		assertNotNull newOrder.orderNumber
	}

	@Test
	void saveOrder_shouldGenerateOrderNumber() {
		when:
		def newOrder = new Order();
		newOrder.name = "Order 1234"
		newOrder.orderType = OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		service.saveOrder(newOrder)
		then:
		assertNotNull newOrder.orderNumber
	}

	@Test
	void saveOrder_shouldNotGenerateOrderNumber() {
		when:
		def newOrder = new Order();
		newOrder.name = "Order 1234"
		newOrder.orderNumber = "PO-12345"
		newOrder.orderType = OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name())
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		service.saveOrder(newOrder)
		then:
		assertEquals "PO-12345", newOrder.orderNumber
	}
}
