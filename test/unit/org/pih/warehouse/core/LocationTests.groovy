package org.pih.warehouse.core

import grails.test.*

class LocationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		def depot = new LocationType(name: "Depot")
		def location1 = new Location(name: "Boston", locationType: depot)
		def location2 = new Location(name: "Miami", locationType: depot)
				
		mockDomain(LocationType, [depot])
		mockDomain(Location, [location1, location2])
    }

    protected void tearDown() {
        super.tearDown()
    }

    void test_shouldHaveLocations() {
		println Location.list()
		assertEquals 2, Location.list().size()		
    }
	
	void test_shouldSaveLocation() {
		def location = new Location(name: "Default location", locationType: new LocationType(name: "Depot"))
		
		if ( !location.validate() )
				location.errors.allErrors.each { println it }
				
		assertTrue location.validate()
		location.save()
		assertFalse location.hasErrors()
	}
	
	void test_shouldHaveLocationType() { 
		def location = new Location()
		assertFalse location.validate()
		println location.errors
		assertEquals "nullable", location.errors["locationType"]
	}
}
