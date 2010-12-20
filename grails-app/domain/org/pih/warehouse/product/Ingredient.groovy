package org.pih.warehouse.product;

import org.pih.warehouse.core.UnitOfMeasure;

/**
 *
 */
class Ingredient {
	
	String inn				// international name
	String strength			// e.g. "200"	
	DosageForm form			// e.g. "tablet" 
	UnitOfMeasure unit		// e.g. "mg"
	    
	static constraints = {
		inn(nullable:true)
		strength(nullable:true)
		unit(nullable:true)
		form(nullable:true)
	}
	
	String toString() { return inn + " " + strength + " " + unit + " " + form ; }
	
	
}
