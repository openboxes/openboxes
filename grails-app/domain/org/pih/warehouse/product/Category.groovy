package org.pih.warehouse.product;

import java.util.Date;

class Category {

	String name
	String description
	Integer sortOrder = 0;
	Category parentCategory
	Date dateCreated;
	Date lastUpdated;
	boolean deleted 
	
	
	static hasMany = [ categories : Category ];
	static mappedBy = [ categories : "parentCategory" ];
	static belongsTo = [ parentCategory : Category ];
	static transients = [ "parents", "children", "deleted", "products" ]
	static mapping = {
		sort name:"desc"
		categories sort:"name"
	}
	
	static constraints = {
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		parentCategory(nullable:true)
	}  
	
	String toString() { return "$name"; }
	

	List getParents() { 
		def parents = []
		getAllParents(this, parents)	
		return (parents? parents.reverse() : []);
	}
	

	def List getAllParents(Category node, List parents) {	
		if (node) { 
			parents << node;
			if (node.parentCategory) {
				getAllParents(node.parentCategory, parents)
			}
			else {
				//return parents;
			}
		}
	}
	
	def getChildren() {
		return categories ? categories*.children.flatten() + categories : []
	}
	
	def getProducts() { 
		try {  
			return Product.findAllByCategory(this);
		} catch (Exception e) { 
			log.info("Error getting products for " + this.name + ".")
			return null;	
		}
		
	}

	


	
}
