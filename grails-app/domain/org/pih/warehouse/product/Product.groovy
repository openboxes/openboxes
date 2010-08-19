package org.pih.warehouse.product

import java.util.Date;

/**
 * An product is an instance of a generic.  For instance,
 * the product might be Ibuprofen, but the product is Advil 200mg
 *
 * We only track products and lots in the warehouse.  Generics help us
 * report on product availability across a generic product like Ibuprofen
 * no matter what size or shape it is.
 */
class Product {

	// Basic attributes	
    String name					// A product name (e.g. a brand name, dosage strength like Advil 200mg)
    String tags					// Comma separated list of tags
	String ean;					// A universal product code (http://en.wikipedia.org/wiki/European_Article_Number)
	String productCode;			// An internal product code
	String description
	Boolean unverified			// A product is mark as unverified if it was added on the fly
	
	// Classification
	Category category			// the product's primary category
    GenericType genericType 	// the generic type of product (e.g. Pain Reliever)
    ProductType productType		// the specific type of product (e.g. Ibuprofen)
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static hasMany = [ categories : Category ]
	                  
    static constraints = {
		name(blank:false) 
		unverified(nullable:true)
		category(nullable:true)       		
		genericType(nullable:true)
		productType(nullable:true)		
		tags(nullable:true)				
        description(nullable:true)
        ean(nullable:true)
		productCode(nullable:true)		
    }

    String toString() { return "$name"; }
    
}

