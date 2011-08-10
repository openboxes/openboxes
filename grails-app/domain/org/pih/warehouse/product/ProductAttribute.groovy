package org.pih.warehouse.product;

/**
 * Represents the value of a particular Attribute for a particular Product
 */
class ProductAttribute {
	
	Attribute attribute;	
	String value;
		
	static belongsTo = [ product : Product ]
	
	static constraints = { 
		attribute(nullable:false)
		value(maxSize: 255)
	}
	
}
