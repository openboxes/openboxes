package org.pih.warehouse.shipping

import grails.test.*
import org.pih.warehouse.core.*
import org.pih.warehouse.shipping.*

class ShipmentTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testReferenceNumbers () {
    	Location loc1 = new Location ([name: "Location 1", id: 1])
    	Location loc2 = new Location ([name: "Location 2", id: 2])
    	
    	ReferenceNumberType refType1 = new ReferenceNumberType ([name: "Type 1", id: 1])
    	ReferenceNumberType refType2 = new ReferenceNumberType ([name: "Type 2", id: 2])
    	
    	ReferenceNumber ref1 = new ReferenceNumber ([id: 1, referenceNumberType: refType1, identifier: "1234"])
      	ReferenceNumber ref2 = new ReferenceNumber ([id: 2, referenceNumberType: refType2, identifier: "5678"])
    	ReferenceNumber ref3 = new ReferenceNumber ([id: 3, referenceNumberType: refType1, identifier: "9012"])
    	
    	Shipment shipment = new Shipment([name: "Test Shipment", id: 1, 
    	                                  expectedShippingDate: new Date(),
    									  expectedDeliveryDate: new Date() + 1, 
    									  origin: loc1, destination: loc2])
    	
    	mockForConstraintsTests(Shipment, [ shipment ])
    	
    	// sanity check that we've defined the shipment correctly
    	assertTrue shipment.validate()
    	
    	// now make sure that we can add valid reference numbers
    	shipment.referenceNumbers = []
    	shipment.referenceNumbers.add(ref1)
    	shipment.referenceNumbers.add(ref2)
    	assertTrue shipment.validate()
    	
    	// now make sure that we can't add another reference number of the same typ
    	shipment.referenceNumbers.add(ref3)
    	assertFalse shipment.validate()
    }
}
