package org.pih.warehouse

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
	
	}

    String toString() { return "$product?.name"; }
    
}
