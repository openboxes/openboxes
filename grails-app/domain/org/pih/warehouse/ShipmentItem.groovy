package org.pih.warehouse

class ShipmentItem implements Comparable {

    Product product		    // Specific product that we're tracking
    Integer quantity		    // Quantity could be a class on its own

    static belongsTo = [ container : Container ]

    static constraints = {
		quantity(min:0, nullable:false)
		product(nullable:false)
		container(nullable:false)
    }
    
    int compareTo(obj) { product.name.compareTo(obj.product.name) }
}
