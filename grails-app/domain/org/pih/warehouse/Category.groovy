package org.pih.warehouse

class Category {

	Category parent

	String code			// i18n code
	String name			
	String description	
	
	static hasMany = [ categories : Category ];
	static mappedBy = [ categories : "parent" ];
	static belongsTo = [ parent : Category ];

	               	                     
	               	                     
	static constraints = {
		code(nullable:true)
		description(nullable:true)
		parent(nullable:true)
	}  
	
	                     
	
	                     
	
}
