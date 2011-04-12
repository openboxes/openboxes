package org.pih.warehouse.product;

class ProductAttribute {
	
	Attribute attribute;	
	String value;
		
	static belongsTo = [ product : Product ]
	//static hasMany = [ values : Value ]				// Values associated with a product and attribute 
	static mapping = { 
		table 'product_attribute'
		values joinTable: [name:'product_attribute_value', column: 'value_id', key: 'product_attribute_id']
	}
	
	static constraints = { 
		attribute(nullable:false)
		value(maxSize: 255)
		//values(nullable:true)
	}
	
}
