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

    // Product codes
    String ean;				// A universal product code (http://en.wikipedia.org/wiki/European_Article_Number)
    String name;
    String description;
    String productCode;		// An internal product code

    ProductType type;		// should be the same as the class (e.g. Product or DrugProduct)
    ProductType subType;	// should be a cascading relationship defined by subclass
        
    // Core association mappings
	static hasMany = [categories : Category, conditionTypes : ConditionType]
    
    static constraints = {
        ean(nullable:true)
        name(blank:false)
        description(nullable:true)
		productCode(nullable:true)
		type(nullable:true)
		subType(nullable:true)	
    }

    String toString() { return "$name"; }

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

    
}
