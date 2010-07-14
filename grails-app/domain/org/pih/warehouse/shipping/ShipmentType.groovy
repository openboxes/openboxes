package org.pih.warehouse.shipping

import org.pih.warehouse.core.Type;

/**
 * Represents the type of shipment (Sea, Air, Suitcase, Domestic Freight, Other)
 */
class ShipmentType extends Type {
	
	
	static hasMany = [ containerTypes : ContainerType ]
	
	static constraints = {
		containerTypes(nullable:true)
	}
}
