package org.pih.warehouse

import grails.test.*
import org.pih.warehouse.inventory.Warehouse;

class WarehouseTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSomething() {
		Warehouse thisWarehouse = new Warehouse();
		thisWarehouse.save()
		assertNotNull(thisWarehouse.id)
	
		Warehouse remoteWarehouse = new Warehouse();
		remoteWarehouse.save()
		assertNotNull(remoteWarehouse.id)
    }
}
