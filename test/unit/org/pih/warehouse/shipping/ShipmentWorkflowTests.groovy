package org.pih.warehouse.shipping

import grails.test.*

class ShipmentWorkflowTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIsExcluded() {
    	ShipmentType shipmentType = new ShipmentType([name:"Some Shipment Type"])
    	ShipmentWorkflow workflow = new ShipmentWorkflow([name:"Some Shipment Workflow",
    	                                                  shipmentType:shipmentType])
    	
    	workflow.excludedFields = "shipmentNumber,expectedShipmentDate,expectedArrivalDate"
    		
    	assert workflow.isExcluded("expectedShipmentDate")
    	assert workflow.isExcluded("shipmentNumber")
    	assert workflow.isExcluded("expectedArrivalDate")
    	assert workflow.isExcluded("ExpecTedshipmeNtDate")
    	assert !workflow.isExcluded("name")
    }
}
