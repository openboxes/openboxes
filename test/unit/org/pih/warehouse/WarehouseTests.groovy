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
		Warehouse localWarehouse = new Warehouse();
		localWarehouse.save()
		assertNotNull(localWarehouse.id)
	
		Warehouse remoteWarehouse = new Warehouse();
		remoteWarehouse.save()
		assertNotNull(remoteWarehouse.id)
    }
}
