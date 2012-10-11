/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryService;

import grails.test.*
import grails.converters.JSON

class LocationControllerTests extends ControllerUnitTestCase {

	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testIndex() {
		controller.index()
		assertEquals "list", controller.redirectArgs["action"]
	}

	void testList() {
		mockDomain(Location, [
			new Location(id: 1, name: "Boston" ),
			new Location(id: 2, name: "Miami")
		])
		def model = controller.list()
		assertEquals 2, model["locationInstanceList"].size()
		assertEquals 2, model["locationInstanceTotal"]
	}

	void testListWithQuery() {
		mockDomain(Location, [
			new Location(id: 1, name: "Boston" ),
			new Location(id: 2, name: "Miami")
		])
		this.controller.params.q = "Bos"
		def model = controller.list()
		assertEquals 1, model["locationInstanceList"].size()
		assertEquals 1, model["locationInstanceTotal"]
	}


	void testShow() {
		def depot = new LocationType(name: "Depot")
		mockDomain(Location, [
			new Location(id: "1", name: "Boston", locationType: depot ),
			new Location(id: "2", name: "Miami", locationType: depot)
		])

		// Mock the inventory service.
		//def location = new Location(id: 1, name: "Boston", locationType: depot)
		//assertTrue location.validate()

		def inventoryControl = mockFor(InventoryService)
		inventoryControl.demand.getLocation(1..1) { locationId -> Location.get(locationId) }

		// 	Initialise the service and test the target method.
		this.controller.inventoryService = inventoryControl.createMock()

		this.controller.params.id = "1"

		def model = this.controller.show()

		
		assertEquals "Boston", model["locationInstance"]?.name
		
	}


}