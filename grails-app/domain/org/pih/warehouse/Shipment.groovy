package org.pih.warehouse

class Shipment {

    // Core data elements
    String status
    String trackingNumber
    Date expectedShippingDate
    Date actualShippingDate

    // Core associations
    Warehouse source
    Warehouse target

    // Core association mappings
    static hasMany = [ shipmentLineItems : ShipmentLineItem, documents : Attachment, products : Product ]

    // Constraints
    static constraints = {
	status(inList:["Order Received", "Shipmment Packed", "Shipment Sent"])
	trackingNumber(nullable:true)
	expectedShippingDate(nullable:true)
	actualShippingDate(nullable:true)
	source(nullable:false)
	target(nullable:false)
	products(nullable:true)
    }
}
