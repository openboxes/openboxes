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

import grails.orm.PagedResultList

// import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryService;

import grails.test.*
// import grails.converters.JSON

class LocationControllerTests extends ControllerUnitTestCase {

	protected void setUp() {
		super.setUp()
		def depot = new LocationType(id: "1", name: "Depot")
		def ward = new LocationType(id: "2", name: "Ward")
		def boston = new LocationGroup(id: "boston", name: "Boston")
		def mirebalais = new LocationGroup(id: "boston", name: "Mirebalais")
		
		mockDomain(Location, [
			new Location(id: "1", name: "Boston", locationType: depot, locationGroup: boston),
			new Location(id: "2", name: "Miami", locationType: depot, locationGroup: boston),
			new Location(id: "3", name: "Mirebalais", locationType: depot, locationGroup: mirebalais),
			new Location(id: "4", name: "Mirebalais > Pediatrics", locationType: ward, locationGroup: mirebalais)
		])
		mockDomain(LocationType, [depot, ward])
		mockDomain(LocationGroup, [boston, mirebalais])

		def locationServiceMock = mockFor(LocationService)
		locationServiceMock.demand.getLocations { locationType, locationGroup, query, max, offset ->
            println "Get locations"
            if (query=="Bos") {
                return new PagedResultList([Location.get(1)], 1)
            }
			return new PagedResultList(Location.list(), 4)
		}

		controller.locationService = locationServiceMock.createMock()
		
		depot = LocationType.get("1")
		assertNotNull depot
	}

	protected void tearDown() {
		super.tearDown()
	}

	void test_index_shouldRedirectToList() {
		controller.index()
		assertEquals "list", controller.redirectArgs["action"]
	}

	void test_list_shouldListAllLocations() {
		def model = controller.list()
		assertEquals 4, model["locationInstanceList"].size()
		assertEquals 4, model["locationInstanceTotal"]
	}

	void test_list_shouldListLocationsMatchingQuery() {
		this.controller.params.q = "Bos"
		def model = controller.list()
		assertEquals 1, model["locationInstanceList"].size()
		assertEquals 1, model["locationInstanceTotal"]
	}

	/*
	void test_list_shouldListLocationsMatchingLocationType() { 
		this.controller.params["locationType"] = "1"
		def model = controller.list()
		assertEquals 3, model["locationInstanceList"].size()
		assertEquals 3, model["locationInstanceTotal"]

		this.controller.params["locationType"] = "2"
		model = controller.list()
		assertEquals 1, model["locationInstanceList"].size()
		assertEquals 1, model["locationInstanceTotal"]

		this.controller.params["q"] = "Bos"
		this.controller.params["locationType"] = "1"
		model = controller.list()
		assertEquals 1, model["locationInstanceList"].size()
		assertEquals 1, model["locationInstanceTotal"]
	}
	*/
		
	void test_show_shouldIncludeLocationInModel() {
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
	
	/*
	void test_uploadLogo_shouldDoSomething() { 
		this.controller.params.id = "1"
		def model = this.controller.uploadLogo()
		
		
		println model 		
	}	
	*/


}