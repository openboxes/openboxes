package org.pih.warehouse.product;

import java.util.Date;

/**
 * A generic type is the idea of something, in the Aristotlean sense
 * (see Theory of Forms).  A generic product might have many formulations,
 * representations, sizes, shapes; but at its core, it's still that one
 * idea of a thing.  For instance, a reclining chair is still just a chair.
 * A blue, medium-sized glove is still just a glove.  This class 
 * encapsulates the generic idea of a product.
 */
class GenericType {
	
	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;
		
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
	}
	
	static mapping = {
		sort "sortOrder"
	}

}
