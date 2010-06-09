package org.pih.warehouse

class ProductAttributeValue {
	
	Boolean allowMultiple
	Attribute attribute;
	
	static belongsTo = [ product : Product ]
	static hasMany = [values : Value]
	                  
	static constraints = { 
		attribute(nullable:false)		
		allowMultiple(nullable:true)
	}
	
}
