package org.pih.warehouse.product;

/**
 * Represents the value of a particular Attribute for a particular Product
 */
class ProductAttribute {

	String id
	Attribute attribute;	
	String value;
		
	static belongsTo = [ product : Product ]
	
	static mapping = {
		id generator: 'uuid'
	}
	
	static constraints = { 
		attribute(nullable:false)
		value(maxSize: 255)
	}
	
}
