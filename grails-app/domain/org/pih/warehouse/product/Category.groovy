package org.pih.warehouse.product;

import java.util.Date;

import org.pih.warehouse.core.Type;

class Category extends Type {

	Category parentCategory
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static hasMany = [ categories : Category ];
	static mappedBy = [ categories : "parentCategory" ];
	static belongsTo = [ parentCategory : Category ];
	
	static mapping = {
		sort name:"desc"
		categories sort:"name"
	}
	

	               	                     
	String toString() { return "$name"; }
	
	static constraints = {
		parentCategory(nullable:true)
	}  
	
	                     
	
	                     
	
}
