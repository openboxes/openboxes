package org.pih.warehouse.core

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location {

    String name
    String logoUrl 
    Address address

    static constraints = {
		logoUrl(nullable:true)
    }
}
