package org.pih.warehouse

class Shipment {

    // Core data elements
    Boolean delivered
    String status
    String comments	
    Date expectedShippingDate
    Date expectedDeliveryDate
    Date actualShippingDate
    Date actualDeliveryDate
    
    // Core associations
    Warehouse source
    Warehouse target

    // Shipping fields
    String trackingNumber
    ShipmentMethod shippingMethod
    ShipmentStatus shippingStatus
    
    // Core association mappings
    static hasMany = [ shipmentLineItems : ShipmentLineItem, documents : Attachment, products : Product ]

    // Constraints
    static constraints = {
    	delivered(nullable:true)
    	comments(nullable:true)
		trackingNumber(nullable:true)
		expectedShippingDate(nullable:true)
		actualShippingDate(nullable:true)
		expectedDeliveryDate(nullable:true)
		actualDeliveryDate(nullable:true)
		source(nullable:false)
		target(nullable:false)
		shippingMethod(nullable:true)
		shippingStatus(nullable:true)
		products(nullable:true)
		shipmentLineItems(nullable:true)
		status(inList:["An order for supplies has been received",		               
		               "The shipmment is being packed", 
		               "The shipmment has been packed",
		               "The shipmment has been loaded onto truck",
		               "The shipment has been sent",
		               "The shipment is in transit",
		               "The shipment should have arrived, but there's no confirmation",
		               "(NOTE:  This is just a sample list of events)"
		               ])
    }
}
