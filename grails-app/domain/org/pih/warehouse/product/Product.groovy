package org.pih.warehouse.product

import java.util.Date;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.UnitOfMeasure;
import org.pih.warehouse.product.Category;

/**
 * An product is an instance of a generic.  For instance,
 * the product might be Ibuprofen, but the product is Advil 200mg
 *
 * We only track products and lots in the warehouse.  Generics help us
 * report on product availability across a generic product like Ibuprofen
 * no matter what size or shape it is.
 * 
 * We will just support "1 unit" for now.  Variations of products will 
 * eventually be stored as product variants (e.g. a 200 count bottle of 
 * 20 mg tablets vs a 50 count bottle of 20 mg tablets will both be stored 
 * as 20 mg tablets).  
 */
class Product implements Serializable {
	
	// Base product information 
	String name;							// Specific description for the product
	String description;						// Not used at the moment
	String productCode 						// Internal product code identifier
	Boolean coldChain = Boolean.FALSE;
	
	// New fields that need to be reviewed
	String upc				// Universal product code
	String ndc				// National drug code
	String manufacturer		// Manufacturer
	String manufacturerCode // Manufacturer product (e.g. catalog code)
	String unitOfMeasure	// each, pill, bottle, box
	
	// Associations 
	Category category;						// primary category
	List attributes = new ArrayList();		// custom attributes
	List categories = new ArrayList();		// secondary categories
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static transients = ["rootCategory"];
	
	static hasMany = [ categories : Category, attributes : ProductAttribute, tags : String ]
	static mapping = {
		categories joinTable: [name:'product_category', column: 'category_id', key: 'product_id']
	}
		
    static constraints = {
		name(nullable:false)
		description(nullable:true)
		productCode(nullable:true)
		unitOfMeasure(nullable:true)
		category(nullable:true)
		coldChain(nullable:true)
		
		upc(nullable:true)
		ndc(nullable:true)
		mfr(nullable:true)
		mfrCode(nullable:true)

    }
	
	def getCategoriesList() {
		return LazyList.decorate(categories,
			  FactoryUtils.instantiateFactory(Category.class))
	}
	
	Category getRootCategory() { 
		Category rootCategory = new Category();
		rootCategory.categories = this.categories;
		return rootCategory;
	}
	
	String toString() { return "$name"; }
    
}

