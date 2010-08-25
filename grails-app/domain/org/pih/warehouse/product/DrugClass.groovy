package org.pih.warehouse.product;

import java.util.Date;

/**
 * A drug may be classified by the chemical type of the active ingredient 
 * or by the way it is used to treat a particular condition.  Each drug can 
 * be classified into one or more drug classes.
 * 
 * See http://www.drugs.com/drug-classes.html?tree=1
 *
 */
class DrugClass {

	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;

	static belongsTo = [ parentDrugClass : DrugClass ]
			
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
	}

	static mapping = {
		sort "sortOrder"
	}

		
}
