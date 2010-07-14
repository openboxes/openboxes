package org.pih.warehouse

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
    String name				// brand name and dosage strength: (e.g. Advil 200mg)
    GenericType genericType // the generic type of product (e.g. Pain Reliever)
    ProductType productType	// the specific type of product (e.g. Ibuprofen)
    Category category
	
	//ProductType subType;	// should be a cascading relationship defined by subclass
    
    String tags				// Comma separated list of tags
    String description
    Boolean markAsImportant		// Mark with the given product is important or not

    // Other attributes
    String unit				// values: tablet, capsule, vial, pill, bottle, injection, box, each, grams, carton, case
    String quantityPerUnit	// quantity per unit
	Float weight
    
    // Product codes
    String ean;				// A universal product code (http://en.wikipedia.org/wiki/European_Article_Number)
    String productCode;		// An internal product code

    // Core association mappings
	static hasMany = [brandNames : String, 
					  categories : Category, 
	                  productAttributeValues : ProductAttributeValue ]
	                  
    static constraints = {
		name(blank:false) 
		category(nullable:true)       		
		genericType(nullable:true)
		productType(nullable:true)		
		tags(nullable:true)		
		weight(nullable:true)
        description(nullable:true)
		markAsImportant(nullable:true)
        unit(nullable:true)
        quantityPerUnit(nullable:true)        
        ean(nullable:true)
		productCode(nullable:true)		
    }

    String toString() { return "$name"; }
    
}

/*
boolean equals(other) {
if (other?.is(this)) return true;
if (!(other instanceof Product)) return false;
if (!id || !other?.id || id != other?.id) return false;
return true;
}*/

/*
int hashCode() {
int hashCode = 0;
hashCode = 29 * (hashCode +  (!id ? 0 : id ^ (id >>> 32)));
}*/
