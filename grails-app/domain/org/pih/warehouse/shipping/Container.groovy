package org.pih.warehouse.shipping;

import java.util.Date;
import org.pih.warehouse.core.Person;

class Container implements Comparable, java.io.Serializable {

	//def beforeDelete = {
	//	shipment.removeFromContainers(this)
	//}

	
	String name	
	String containerNumber				// An official container number (if it exists)
	String description					// Description of contents
	Person recipient					// Person who is assigned to receive the container
	Integer sortOrder					// 
	Float height;						// height of container
	Float width;						// width of container
	Float length;						// length of container 
	String volumeUnits					// standard dimensional unit: cm, in, ft, 	
	Float weight						// weight of container
	String weightUnits					// standard weight unit: kg, lb
	Date dateCreated;
	Date lastUpdated;
	
	ContainerType containerType			// Type of container
	ContainerStatus containerStatus		// Status of the container (open, closed)
	
	//Shipment shipment
	Container parentContainer			// the "containing" container
	SortedSet containers				// Child containers (in combination with mapping, helps to order containers)

	//static belongsTo = [ parentContainer : Container ]
	static belongsTo = [ shipment : Shipment ];
	static hasMany = [ containers : Container ];
	//static mappedBy = [containers: 'parentContainer']

	static transients = [ "optionValue", "shipmentItems" ]
	static mapping = {
		//containers sort: 'sortOrder', order: 'asc'
		containers cascade: "all-delete-orphan"
	}
		
	// Constraints
	static constraints = {	 
		name(nullable:false)
		description(nullable:true)
		containerNumber(nullable:true)
		parentContainer(nullable:true)
		recipient(nullable:true)
		height(nullable:true)
		width(nullable:true)
		length(nullable:true)
		volumeUnits(nullable:true)
		weight(nullable:true)
		weightUnits(nullable:true)
		containerType(nullable:false)
		//shipmentItems(nullable:true)		
		//parentContainer(nullable:true)
		containerStatus(nullable:true)
		sortOrder(nullable:true)
	}	

	int compareTo(obj) { 
		return sortOrder.compareTo(obj.sortOrder) 
	}
	
	List<ShipmentItem> getShipmentItems() { 
		return ShipmentItem.findAllByContainer(this)
	}
	
	String getOptionValue() {
		return containerType.name + "-" + name
	}
	
}
