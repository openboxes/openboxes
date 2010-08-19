package org.pih.warehouse.product;

class ProductAttributeValue {
	
	Attribute attribute;
	Boolean allowMultiple
	
	static belongsTo = [ product : Product ]
	static hasMany = [values : Value]
	                  
	static constraints = { 
		attribute(nullable:false)		
		allowMultiple(nullable:true)
	}
	
}
