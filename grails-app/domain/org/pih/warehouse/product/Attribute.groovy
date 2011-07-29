package org.pih.warehouse.product

import java.util.Date;

/**
 * Simple implementation of entity-attribute-value model that allows for 
 * a Product to be extended to contain custom attribute values
 * TODO: This should really be named ProductAttribute
 */
class Attribute {
	
	String name 			// The name of the attribute (e.g. 'vitality')
	Boolean allowOther		// If true, supports a free-text entry for value
	List options;			// Valid coded option values for this attribute
	
	Date dateCreated;
	Date lastUpdated;

	static hasMany = [options : String ];
	
	static constraints = { 
		name(nullable:false, maxSize: 255)
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	       
	String toString() { return "$name"; }
}
