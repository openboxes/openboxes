package org.pih.warehouse.product;

/**
 * Represents the value of a particular Attribute for a particular Product
 * TODO: We should really name this class ProductAttributeValue
 */
class ProductAttribute {
	
	Attribute attribute;	
	String value;
		
	static belongsTo = [ product : Product ]
	static mapping = { 
		table 'product_attribute'
		values joinTable: [name:'product_attribute_value', column: 'value_id', key: 'product_attribute_id']
	}
	
	static constraints = { 
		attribute(nullable:false)
		value(maxSize: 255)
	}
	
}
