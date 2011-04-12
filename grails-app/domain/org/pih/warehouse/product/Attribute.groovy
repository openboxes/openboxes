package org.pih.warehouse.product

import java.util.Date;
import org.pih.warehouse.core.DataType;


/* 
 * Simple implementation of entity-attribute-value model.
 */
class Attribute {
	
	String name 				// The name of the attribute (e.g. 'vitality')
	Boolean allowOther
	
	//DataType dataType 			// The expected data type of the value (e.g. Integer, Float, String, Date)
	//Value defaultValue			// The default value 
	//String label				// A display string ('Vitality of product')
	//Boolean allowMultiple		// Indicates whether this attribute allows multiple options to be selected
	List options;
	
	Date dateCreated;
	Date lastUpdated;
		
	//static hasMany = [ options : Value ];		// Available options (e.g. 'essential', 'vital', 'normal')	
	static hasMany = [options : String ];
	
	//static mapping = { 
		//values joinTable: [name: 'attribute_value', column: 'value_id', key: 'attribute_id']			
	//}
	
	static constraints = { 
		name(nullable:false, maxSize: 255)
		//label(nullable:true)
		//dataType(nullable:true)
		//defaultValue(nullable:true)
		//allowMultiple(nullable:true)
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	       
	String toString() { return "$name"; }
	                  
	
}
