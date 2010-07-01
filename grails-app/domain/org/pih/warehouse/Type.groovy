package org.pih.warehouse

class Type {	

	String name
	String color
	String description
	Integer sortOrder = 0;
	
	static constraints = { 
		name(nullable:false)
		color(nullable:true)
		description(nullable:true)		
		sortOrder(nullable:true)
	}

	static mapping = {
		sort "sortOrder"
	}
	
	String toString() { return "$name"; }	
	
}
