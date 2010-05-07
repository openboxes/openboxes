package org.pih.warehouse

class ShipmentLineItem {

    Product product		    // Specific product that we're tracking
    Integer quantity		    // Quantity could be a class on its own

    static belongsTo = [ shipment : Shipment ]

    static constraints = {
	quantity(min:0, nullable:false)
	product(nullable:false)
	shipment(nullable:false)

    }
}
