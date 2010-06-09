package org.pih.warehouse

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
class DrugProduct extends ConsumableProduct {
	
    String genericName		// generic name: Ibuprofen 
	String dosageStrength	// e.g. "200 mg"	
    String dosageForm		// e.g. "tablet" 
    String dosageRegimen	// e.g. "Take two capsules by mouth daily"
    String drugClass 		// should be a separate object (and needs to be very expressive)

    PackageType packageType			// e.g. bottle, vial
    DrugRouteType drugRouteType		// route of administration (oral, rectal, etc)
    DrugClass majorDrugClass 		// replaces drug class string above
    
	
	static hasMany = [ conditionTypes : ConditionType ]
    
    static constraints = {
    	genericName(nullable:true)
		dosageStrength(nullable:true)
    	dosageForm(nullable:true)
    	dosageRegimen(nullable:true)
    	drugClass(nullable:true)
    	packageType(nullable:true)
    	drugRouteType(nullable:true)
    	majorDrugClass(nullable:true)
    }
}
