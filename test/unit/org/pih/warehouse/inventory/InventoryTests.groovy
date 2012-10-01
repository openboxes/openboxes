/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
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
