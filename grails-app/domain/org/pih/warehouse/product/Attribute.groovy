package org.pih.warehouse.product

import java.util.Date;
import org.pih.warehouse.core.DataType;


/* 
 * Simple implementation of entity-attribute-value model.
 */
class Attribute {
	
	String name 				// The name of the attribute (e.g. 'vitality')
	DataType dataType 			// The expected data type of the value (e.g. Integer, Float, String, Date)
	Value defaultValue			// The default value 

	String label				// A display string ('Vitality of product')
	Boolean allowMultiple		// Indicates whether this attribute allows multiple options to be selected
	
	Date dateCreated;
	Date lastUpdated;
		
	static hasMany = [ options : Value ];		// Available options (e.g. 'essential', 'vital', 'normal')
	
	//static mapping = { 
		//values joinTable: [name: 'attribute_value', column: 'value_id', key: 'attribute_id']			
	//}
	
	static constraints = { 
		name(nullable:false)
		label(nullable:true)
		dataType(nullable:true)
		defaultValue(nullable:true)
		allowMultiple(nullable:true)
	}
	       
	                  
	                  
	
}
