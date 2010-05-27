package org.pih.warehouse

class Container implements Comparable {

	String name
	Float weight 		
	String units		// should probably be a class on its own
	ContainerType containerType
	SortedSet shipmentItems
	
	static hasMany = [ shipmentItems : ShipmentItem ];
	static belongsTo = [ shipment : Shipment ];
	
	// Constraints
	static constraints = {	 
		name(nullable:true)
		weight(nullable:true)
		units(nullable:true)
		containerType(nullable:true)
		shipment(nullable:true)
		shipmentItems(nullable:true)		
	}
	
	int compareTo(obj) { name.compareTo(obj.name) }
	
}
