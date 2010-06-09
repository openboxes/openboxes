package org.pih.warehouse

class Category extends Type {

	Category parent
	
	static hasMany = [ categories : Category ];
	static mappedBy = [ categories : "parent" ];
	static belongsTo = [ parent : Category ];

	               	                     
	String toString() { return "$name"; }
	
	static constraints = {
		parent(nullable:true)
	}  
	
	                     
	
	                     
	
}
