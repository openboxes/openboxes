package org.pih.warehouse.shipping

/**
 * Replaces the Container class.
 *
 */
class ShipmentPackage implements Comparable {

	String name
	Boolean status = true		// open (true) or closed (false)	
	String unitNumber
	String dimensions			// could be its own class, but we don't care right now
	Float weight	
	String units				// should probably be a class on its own
	
	ContainerType containerType
	SortedSet shipmentItems
	
	static hasMany = [ shipmentItems : ShipmentItem ];
	static belongsTo = [ shipment : Shipment ];
	
	// Constraints
	static constraints = {	 
		name(nullable:true)
		unitNumber(nullable:true)
		dimensions(nullable:true)
		weight(nullable:true)
		units(nullable:true)
		containerType(nullable:true)
		//shipment(nullable:true)
		shipmentItems(nullable:true)		
	}
	
	int compareTo(obj) { name.compareTo(obj.name) }
	
}
