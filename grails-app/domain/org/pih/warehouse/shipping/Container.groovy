package org.pih.warehouse.shipping;

import java.util.Date;
import org.pih.warehouse.core.Person;

class Container implements Comparable {

	String name	
	String containerNumber
	String description
	Person recipient

	Container parentContainer
	
	// Dimensions
	Float height;				// height of container
	Float width;				// width of container
	Float length;				// length of container 
	String volume_units			// standard dimensional unit: cm, in, ft, 
	
	// Weight
	Float weight				// weight of container
	String weight_units			// standard weight unit: kg, lb
	
	// Items in container 
	List shipmentItems
	
	// Type of container 
	ContainerType containerType
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static transients = [ "optionValue" ]
	static hasMany = [ shipmentItems : ShipmentItem ];
	static belongsTo = [ shipment : Shipment ];
	
	// Constraints
	static constraints = {	 
		name(nullable:true)
		description(nullable:true)
		containerNumber(nullable:true)
		recipient(nullable:true)
		height(nullable:true)
		width(nullable:true)
		length(nullable:true)
		volume_units(nullable:true)
		weight(nullable:true)
		weight_units(nullable:true)
		containerType(nullable:true)
		shipmentItems(nullable:true)		
		parentContainer(nullable:true)
	}
	
	int compareTo(obj) { name.compareTo(obj.name) }
	
	String getOptionValue() {
		return containerType.name + " #" + name
	}
	
}
