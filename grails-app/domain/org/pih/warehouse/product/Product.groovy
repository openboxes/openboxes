package org.pih.warehouse.product

import java.util.Date;
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
	String name;				// A display name for the product
	String description;			// A description for the product
	String productCode;			// An internal product code
	ProductType productType		// the specific type of product
	ProductClass productClass;	// The class of the product
	
	// Consumable information	
	String brandName
	Boolean coldChain = Boolean.FALSE;
	// Drug information 
	String inn						// international name
	String dosageStrength			// e.g. "200"
	//String dosageUnit				// e.g. "MG"
	DosageForm dosageForm			// e.g. "tablet"
	UnitOfMeasure unitOfMeasure		// e.g. "mg"

	// Durable information 	
	String make 
	String model
	String year 

	// Associations 
	List attributes;
	List categories;
	List ingredients;
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static transients = ["rootCategory"];
	
	static hasMany = [ categories : Category, attributes : ProductAttribute, ingredients : Ingredient ]
	static mapping = {
		table 'product'
		tablePerHierarchy false
		categories joinTable: [name:'product_category', column: 'category_id', key: 'product_id']
		//productNames joinTable: [name:'product_name', column: 'product_name_id', key: 'product_id']
	}
	
    static constraints = {
		// Basic
		name(nullable:true)
		description(nullable:true)
		productCode(nullable:true)		
		productType(nullable:false)		
		productClass(nullable:false)
		// Drug
		dosageStrength(nullable:true)
		dosageForm(nullable:true)
		unitOfMeasure(nullable:true)
		// Consumable
		inn(nullable:true)
		brandName(nullable:true)
		coldChain(nullable:true)
		// Durable goods
		make(nullable:true)
		model(nullable:true)
		year(nullable:true)
		
		//ingredients validator: { value, obj, errors ->
		//	if (val?.size() > 0 && !obj.productClass == ProductClass.DRUG)
		//		errors.rejectValue();
		//}

		// Example of a custom validator 
		//fieldName validator: { value, obj, errors ->
		//	def customValidator = CustomValidator.getInstance()
		//	if (!customValidator.isValid(value)) {
		//		// call errors.rejectValue(), or return false, or return an error code
		//		errors.rejectValue();
		//	}
		//}

    }
	
	
	Category getRootCategory() { 
		Category rootCategory = new Category();
		rootCategory.categories = this.categories;
		return rootCategory;
	}

	/*
	String getName() { 	
		String name = "";	
		
		if (!id) { 
			return "New Product"
		}		
		else { 
			switch(productClass) {
				case ProductClass.DRUG :
					name = inn;
					break; 
				case ProductClass.DURABLE:
					name = make + " " + model + " " + year;
					break;			
				case ProductClass.CONSUMABLE:  
					name = inn;
					break;
				default: 
					"New Product"
			}		
		}		
	}
	*/
    
}

