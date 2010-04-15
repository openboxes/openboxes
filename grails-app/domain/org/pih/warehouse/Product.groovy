package org.pih.warehouse


/**
 * A product is a generic 'thing', in the Aristotlean sense
 * (see Theory of Forms).  A product might have many formulations or
 * representations, but at its core, it's still 'that thing' (e.g. a chair
 * is a chair).
 *
 *
 */
class Product {

    Integer id;
    String upc;		// needs to move to the ProductInstance class
    String name;
    String description;
    String category;
    User user;
    StockCard stockCard;

    //static belongsTo = Category
    //Category category

    static hasOne = [StockCard]

    static constraints = {
        name(blank:false);
	stockCard(blank:true)
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
