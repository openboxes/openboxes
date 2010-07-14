package org.pih.warehouse.product;

import org.pih.warehouse.core.Type;

class ProductType extends Type {

    ProductType parent // if this is a subtype, then parent will not be null
    
    static constraints = {
    	parent(nullable:true)    
    }
}
