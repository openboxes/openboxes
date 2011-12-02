package org.pih.warehouse.shipping;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

class ShipmentServiceTests {

	@Before
	public void setUp() throws Exception {
		def shipment = new Shipment(name: "Test Shipment").save(failOnError:true);
	}

	@Test 
	void testSaveShipment() throws Exception { 
		def shipment = Shipment.findByName("Test Shipment");
		assertNotNull shipment;
	}
	
	@After
	public void tearDown() throws Exception {	
		Shipment.findByName("Test Shipment").delete();
	}
}
