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

    Integer id;
    String name;
    String description;

    // Product codes
    String ean;			// A universal product code (http://en.wikipedia.org/wiki/European_Article_Number)
    String productCode		// An internal product code


    // Needs to be fleshed out a bit more
    String category;
    String subCategory;


    // Needs to be removed
    User user;
    StockCard stockCard;	// should not have a reference to a stock card

    //static belongsTo = Category
    //Category category

    static hasOne = [ StockCard ]

    static constraints = {
        name(blank:false);
	stockCard(nullable:true)
	productCode(nullable:true)
	category(nullable:true)
	subCategory(nullable:true)
	user(nullable:true)
	//size(inList:["Small", "Medium", "Large", "X-Large"]);
	//category(inList:["Clothing", "Equipment", "Other"]);
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
