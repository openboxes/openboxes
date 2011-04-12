package org.pih.warehouse.core

class UnitOfMeasure {

	String name					// unit of measure name (cubic millimeters)
	String code					// abbreviation (e.g. mm3
	String description			// description of unit of measure (optional)
	UnitOfMeasureType type		// area, volume, length, weight, currency
	
	static constraints = { 
		name(nullable:false, maxSize:255)
		code(nullable:false, maxSize:255)
		type(nullable:false)
		description(nullable:true, maxSize:255)
	}
	
	
	String toString() { return "${name}" } 
	
}
