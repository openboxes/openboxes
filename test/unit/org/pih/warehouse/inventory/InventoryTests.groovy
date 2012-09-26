package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;

import grails.test.*

class InventoryTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		def depot = new LocationType(name: "Depot")
		mockDomain(LocationType, [depot])
		
		Location loc1 = new Location ([id: "1", name: "Location 1", locationType: depot])
		Location loc2 = new Location ([id: "2", name: "Location 2", locationType: depot])
		mockDomain(Location, [loc1, loc2])

		mockDomain(Inventory)
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSaveInventory() {
		def inventory = new Inventory();
		def location = Location.get("1")
		inventory.warehouse = location 
		inventory.save()
		assertNotNull inventory.id
    }
}
