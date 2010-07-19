package org.pih.warehouse.product;

import org.pih.warehouse.core.Type;

/**
 * Represents the different types of medical conditions.
 */
class ConditionType extends Type {

	static constraints = { 
		
	}

	String toString() { return "$name"; }	
	
}
