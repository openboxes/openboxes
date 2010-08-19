package org.pih.warehouse.product

import java.util.Date;


/* 
 * Simple implementation of entity-attribute-value model.
 */
class Attribute {
	
	String name 
	String dataType 

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
		
	static hasMany = [values : Value];
		
	static constraints = { 
		name(nullable:false)
		dataType(nullable:true)
	}
	       
	                  
	                  
	
}
