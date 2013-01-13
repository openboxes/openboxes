/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product

import java.util.Date;
import java.util.Collection;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.auth.AuthService;
import org.pih.warehouse.core.*;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.inventory.*;
import org.pih.warehouse.shipping.ShipmentItem;

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
class Product implements Comparable, Serializable {
	
	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}

	// Base product information 
	String id	
	String name;							// Specific description for the product
	String description;						// Not used at the moment
	String productCode 						// Internal product code identifier
	Boolean coldChain = Boolean.FALSE;
	
	// New fields that need to be reviewed
	String upc				// Universal product code
	String manufacturer		// Manufacturer
	String manufacturerCode // Manufacturer product (e.g. catalog code)
	String unitOfMeasure	// each, pill, bottle, box
	UnitOfMeasure defaultUom
	//Integer uomQuantity 

	// NDC attributes
	String ndc				
	// Figure out how we want to handle multiple names 
	/*
	String genericName				// same as the non-proprietary name
	String proprietaryName			// a brand name or trademark under which a proprietary product is marketed
	String nonProprietaryName		// a short name coined for a drug or 
									// chemical not subject to proprietary (trademark) 
									// rights and recommended or recognized by an official body
	String pharmacyEquivalentName	// PEN a shortened name for a drug or combination of drugs; 
									// when used for a combination of drugs, the term usually 
									// consists of the prefix co- plus an abbreviation for each 
									// drug in the combination.
	*/
	
	//String route
	//String dosageForm
		
	// Associations 
	Category category;						// primary category
	List attributes = new ArrayList();		// custom attributes
	List categories = new ArrayList();		// secondary categories
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	User createdBy
	User updatedBy
	
		
	static transients = ["rootCategory", "images"];
	
	static hasMany = [ 
		categories : Category, 
		attributes : ProductAttribute, 
		tags : Tag, 
		documents : Document, 
		productGroups: ProductGroup, 
		packages : ProductPackage, 
		inventoryItems : InventoryItem 
	]	
	
	static mapping = {
		id generator: 'uuid'
		tags joinTable: [name:'product_tag', column: 'tag_id', key: 'product_id'], cascade: 'all-delete-orphan'
		categories joinTable: [name:'product_category', column: 'category_id', key: 'product_id']
		attributes joinTable: [name:'product_attribute', column: 'attribute_id', key: 'product_id']
		documents joinTable: [name:'product_document', column: 'document_id', key: 'product_id']
		productGroups joinTable: [name:'product_group_product', column: 'product_group_id', key: 'product_id']
	}
		
    static constraints = {
		name(nullable:false, blank: false, maxSize: 255)
		description(nullable:true)
		productCode(nullable:true, maxSize: 255, unique: false)
		unitOfMeasure(nullable:true, maxSize: 255)
		category(nullable:false)
		coldChain(nullable:true)
		
		defaultUom(nullable:true)
		upc(nullable:true, maxSize: 255)
		ndc(nullable:true, maxSize: 255)
		manufacturer(nullable:true, maxSize: 255)
		manufacturerCode(nullable:true, maxSize: 255)
		
		//route(nullable:true)
		//dosageForm(nullable:true)
		
		createdBy(nullable:true)
		updatedBy(nullable:true)

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
	
	Collection getImages() { 
		return documents?.findAll { it.contentType.startsWith("image") }
	}
	
	String toString() { return "$name"; }
	
	/**
	* Sort by name
	*/
	int compareTo(obj) {
		this.name <=> obj.name
	}
	
	String tagsToString() { 
		if (tags) { 
			return tags.sort { it.tag }.collect { it.tag }.join(",")
		}
		else { 
			return null
		}
		
	}

  Date latestInventoryDate(def locationId){
    def inventory = Location.get(locationId).inventory 
    def date = TransactionEntry.executeQuery("select max(t.transactionDate) from TransactionEntry as te  left join te.inventoryItem as ii left join te.transaction as t where ii.product= :product and t.inventory = :inventory and t.transactionType.transactionCode in (:transactionCodes)", [product: this, inventory: inventory, transactionCodes:[TransactionCode.PRODUCT_INVENTORY, TransactionCode.INVENTORY]]).first()
    return date
  }
	
	
	/**
	 * Some utility methods
	 */
	
	/**
	 * Returns true if there are any transaction entries or shipment items in the system associated with this product, false otherwise
	 */
	Boolean hasAssociatedTransactionEntriesOrShipmentItems() {
		def items = InventoryItem.findAllByProduct(this)
		if (items && items.find { TransactionEntry.findByInventoryItem(it) } ) { return true }
		if (ShipmentItem.findByProduct(this)) { return true }
		return false
	}

    @Override
	int hashCode() {
		if (this.id != null) {
			return this.id.hashCode();
		}
		return super.hashCode();
	}

    @Override
	boolean equals(Object o) {
		if (o instanceof Product) {
			Product that = (Product)o;
			return this.id == that.id;
		}
		return false;
	}

    Map toJson(){
        [
            id: id,
            name: name,
        ]
    }
}

