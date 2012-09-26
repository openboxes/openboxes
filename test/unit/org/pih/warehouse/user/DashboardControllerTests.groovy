package org.pih.warehouse.user
import org.pih.warehouse.core.Location;

import grails.test.*
import grails.converters.JSON

class DashboardControllerTests extends ControllerUnitTestCase {
	
	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testIndex() {
		
		println controller
		println controller.redirectArgs
		
		//mockDomain(Item, [ new Item(name: "item1"), new Item(name: "item2")] )
		//def model = controller.list()
		//assertEquals 2, model.itemInstanceList.size()
		//mockDomain(Location, [new Location(id: 1, name: "Boston" ), new Location(id: 2, name: "Miami")])
		//def model = controller.list()		
		//assertEquals 2, model.locations.size()
		//assertEquals "list" model.view
	}	
}