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

    String name					// A product name (e.g. a brand name, dosage strength like Advil 200mg)
    String upc;					// A universal product code (http://en.wikipedia.org/wiki/European_Article_Number)
	String productCode;			// An internal product code
	String description
	String imageSrc				// An image of the product
	Boolean unverified			// A product is mark as unverified if it was added on the fly
	
	Category category			// the product's primary category
    GenericType genericType 	// the generic type of product (e.g. Pain Reliever)
    ProductType productType		// the specific type of product (e.g. Ibuprofen)
	
	Date dateCreated;
	Date lastUpdated;
	
	static hasMany = [ categories : Category, tags : String, productNames : ProductName ]
	static mapping = {
		table 'product'
		tablePerHierarchy false
		categories joinTable: [name:'product_category', column: 'category_id', key: 'product_id']
		productNames joinTable: [name:'product_name', column: 'product_name_id', key: 'product_id']
	}

    static constraints = {
		name(blank:false) 
        upc(nullable:true)
		productCode(nullable:true)		
        description(nullable:true)
		imageSrc(nullable:true)
		unverified(nullable:true)
		category(nullable:true)       		
		genericType(nullable:true)
		productType(nullable:true)		
    }

    String toString() { return "$name"; }
    
}

