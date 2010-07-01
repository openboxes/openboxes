package org.pih.warehouse

/**
 * Represents the type of shipment (Sea, Air, Suitcase, Domestic Freight, Other)
 */
class ShipmentType extends Type {
	
	
	static hasMany = [ containerTypes : ContainerType ]
	
	static constraints = {
		containerTypes(nullable:true)
	}
}
