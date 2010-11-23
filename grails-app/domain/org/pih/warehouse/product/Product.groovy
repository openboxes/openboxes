package org.pih.warehouse.product

import java.util.Date;
import org.pih.warehouse.accounting.*;
import org.pih.warehouse.core.UnitOfMeasure;

/**
 * An product is an instance of a generic.  For instance,
 * the product might be Ibuprofen, but the product is Advil 200mg
 *
 * We only track products and lots in the warehouse.  Generics help us
 * report on product availability across a generic product like Ibuprofen
 * no matter what size or shape it is.
 */
class Product implements Serializable {

	// Basic Details 
    String name					// A product name (e.g. a brand name, dosage strength like Advil 200mg)
	String frenchName			// Temporary column until we can support multiple languages
	String code					// A code used for internationalization
	String description			// Description of the product
	Boolean unverified			// A product is mark as unverified if it was added on the fly

	// Product identification codes -- should go into a separate table 
	String ean;					// See http://en.wikipedia.org/wiki/European_Article_Number
    String upc;					// A universal product code 
	String sku;					// Stock keeping unit  	
	String productCode;			// An internal product code	

	// Quantity information
	Integer quantityPerUnit		// Quantity per unit 
	UnitOfMeasure unitOfMeasure	// Unit of measure
	
	// Classifications
	Category category			// the product's primary category
    ProductType productType		// the specific type of product (e.g. Ibuprofen)
	ProductGroup productGroup	// the product's grouping (HIV, TB, Injectable, Lab Supply, Med Supply)
    GenericType genericType 	// the generic type of product (e.g. Pain Reliever)

	// Accounting codes
	BudgetCode budgetCode		// the Serenic code for this product
	//AccountCode accountCode
	//ProgramCode programCode	// the program code for this product
	//ProjectCode projectCode
	//PurposeCode purposeCode 		
	
	// Auditing
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
		frenchName(nullable:true)
		code(nullable:true)
		ean(nullable:true)
        upc(nullable:true)
		sku(nullable:true)		
		productCode(nullable:true)		
		budgetCode(nullable:true)
        description(nullable:true)
		quantityPerUnit(nullable:true)
		unitOfMeasure(nullable:true)
		unverified(nullable:true)
		category(nullable:true)       		
		genericType(nullable:true)
		productType(nullable:true)		
		productGroup(nullable:true)
    }

    String toString() { return "$name"; }
    
}

