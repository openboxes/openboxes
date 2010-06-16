package org.pih.warehouse

class Category extends Type {

	Category parent
	
	static hasMany = [ categories : Category ];
	static mappedBy = [ categories : "parent" ];
	static belongsTo = [ parent : Category ];
	
	static mapping = {
		sort name:"desc"
		categories sort:"name"
	}
	

	               	                     
	String toString() { return "$name"; }
	
	static constraints = {
		parent(nullable:true)
	}  
	
	                     
	
	                     
	
}
