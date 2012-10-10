/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping


import grails.test.*
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;

class ShipmentTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		def depot = new LocationType(name: "Depot")
		mockDomain(LocationType, [depot])
		
    	Location loc1 = new Location ([id: "1", name: "Location 1", locationType: depot])
    	Location loc2 = new Location ([id: "2", name: "Location 2", locationType: depot])
		mockDomain(Location, [loc1, loc2])
		
		def shipmentType = new ShipmentType([id: "1", name: "Sea"])
		mockDomain(ShipmentType, [shipmentType])
		Shipment shipment = new Shipment([id: "1",
			name: "Test Shipment",
			shipmentType: shipmentType,
			expectedShippingDate: new Date(),
			expectedDeliveryDate: new Date() + 1,
			origin: loc1, 			
			destination: loc2])
		
		mockDomain(Shipment, [shipment])
		
		ReferenceNumberType refType1 = new ReferenceNumberType ([name: "Type 1", id: "1"])
		ReferenceNumberType refType2 = new ReferenceNumberType ([name: "Type 2", id: "2"])
		mockDomain(ReferenceNumberType, [refType1, refType2])		
    }
	
	protected void tearDown() {
		super.tearDown()
	}

	void testNullable() { 
		def shipment = new Shipment()
		assertFalse shipment.validate()
		assertEquals "nullable", shipment.errors["name"]
		assertEquals "nullable", shipment.errors["origin"]
		assertEquals "nullable", shipment.errors["destination"]
		assertEquals "nullable", shipment.errors["shipmentType"]
		assertEquals "nullable", shipment.errors["expectedShippingDate"]
		assertNull shipment.errors["recipient"]
		assertEquals shipment.errors.errorCount, 5
		
	}
	
	void testBlank() { 
		def shipment = new Shipment(name: '')
		assertFalse shipment.validate()
		assertEquals 'Name is blank.', 'blank', shipment.errors['name']
	}
	
	void testValidateOnValidShipment() {
		def shipment = new Shipment(
			name: "Test Shipment", 
			shipmentType: new ShipmentType(name: "Test Shipment Type"), 
			origin: new Location(name: "Location 1"),
			destination: new Location(name: "Location 2"),
			expectedShippingDate: new Date())
		
		assertTrue shipment.validate()
	}	
	
	
	void testValidateShouldFailWhenOriginEqualsDestination() { 
		def location = Location.get(1)		
		def shipmentType = new ShipmentType([id: "1", name: "Test Shipment Type"])
		def shipment = new Shipment(
			name: "Test Shipment",
			shipmentType: shipmentType,
			origin: location,
			destination: location,
			expectedShippingDate: new Date())
		
		assertFalse shipment.validate()
		assertEquals "validator", shipment.errors["origin"]
	}
		
	
	void testGetShipmentShouldReturnNotNullShipment() { 
		def shipment = Shipment.get("1")
		assertNotNull shipment		
	}
	
	void testAddReferenceNumbersToShipment() {    	
		def refType1 = ReferenceNumberType.get("1")
		assertNotNull refType1
		
		def refType2 = ReferenceNumberType.get("2")
		assertNotNull refType2
		
		ReferenceNumber ref1 = new ReferenceNumber ([id: "1", referenceNumberType: refType1, identifier: "1234"])
  		ReferenceNumber ref2 = new ReferenceNumber ([id: "2", referenceNumberType: refType2, identifier: "5678"])
		ReferenceNumber ref3 = new ReferenceNumber ([id: "3", referenceNumberType: refType1, identifier: "9012"])
	
		def shipment = Shipment.get("1")
		assertNotNull shipment
		
		// sanity check that we've defined the shipment correctly
		assertTrue shipment.validate()    	
		println shipment.errors		
		
    	// now make sure that we can add valid reference numbers
    	//shipment.referenceNumbers = []
    	shipment.addToReferenceNumbers(ref1)
    	shipment.addToReferenceNumbers(ref2)
    	assertTrue shipment.validate()
    	
    	// now make sure that we can't add another reference number of the same type
    	shipment.addToReferenceNumbers(ref3)
    	assertFalse shipment.validate()	
		
    }
	
	
	
	
}
