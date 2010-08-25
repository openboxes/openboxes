package org.pih.warehouse.product;

/**
 * Drug information that should be included:
 * - Product Trade Name or Catalog Name
 * - National Drug Code (unique 11 digit, 3 segment number). The segments represent the labeler code, the product code and the package code.
 * - Dosage Form
 * - Routes of Administration
 * - Active Ingredients
 * - Strength
 * - Unit
 * - Package Size and Type
 * - Major Drug Class
 * 
 * 
 * Recommended reading:
 * 	- http://www.fda.gov/Drugs/DevelopmentApprovalProcess/UCM070829
 * 	- http://en.wikipedia.org/wiki/National_Drug_Code
 */
class DrugProduct extends Product {
	
	String name				// generic name: Ibuprofen 
	String dosageStrength			// e.g. "200"	
	String dosageUnit			// e.g. "MG"	
	DosageForm dosageForm			// e.g. "tablet" 
	String packageSize			// e.g. 100 
	PackageType packageType			// e.g. bottle, vial
	DrugClass drugClass 			// e.g. antiviral agents
	DrugRouteType drugRouteType		// route of administration (oral, rectal, etc)
    	
	static mapping = {
		table "drug_product"
	}
	
	//static hasMany = [ conditionTypes : ConditionType ]
    
	static constraints = {
		name(nullable:true)
		dosageStrength(nullable:true)
		dosageUnit(nullable:true)
		dosageForm(nullable:true)
		packageSize(nullable:true)
		packageType(nullable:true)
		drugClass(nullable:true)
		drugRouteType(nullable:true)
	}
}
