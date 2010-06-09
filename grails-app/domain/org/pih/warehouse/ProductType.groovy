package org.pih.warehouse

class ProductType extends Type {

    ProductType parent // if this is a subtype, then parent will not be null
    
    static constraints = {
    	parent(nullable:true)    
    }
}
