package org.pih.warehouse

class ProductType {

    String name
    ProductType parent // if this is a subtype, then parent will not be null
    
    static constraints = {
    	parent(nullable:true)    
    }
}
