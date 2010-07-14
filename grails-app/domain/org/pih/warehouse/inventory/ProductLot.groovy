package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;

/**
 * We only track products and lots in the warehouse.  Generics help us
 * report on product availability across a generic product like Ibuprofen
 * no matter what size or shape it is.
 */
class ProductLot {	
	
	String lotNumber
	Date expirationDate
	Product product
	
    static constraints = {
		lotNumber(nullable:false)
		expirationDate(nullable:true)
		product(nullable:false)		
	}

    String toString() { return "$lotNumber for $product?.name expires on $expirationDate"; }
    
}
