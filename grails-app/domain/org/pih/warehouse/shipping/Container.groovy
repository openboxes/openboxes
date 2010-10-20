package org.pih.warehouse.shipping;

import java.util.Date;
import org.pih.warehouse.core.Person;

class Container implements Comparable, java.io.Serializable {

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
	 
	List shipmentItems					// Items in container
	Container parentContainer			// the "containing" container
	ContainerType containerType			// Type of container
	ContainerStatus containerStatus		// Status of the container (open, closed)
	
	static transients = [ "optionValue" ]
	static belongsTo = [ shipment : Shipment];	/* parentContainer : Container */
	static hasMany = [ shipmentItems : ShipmentItem, containers: Container ];
	static mappedBy = [containers: 'parentContainer']	
	
	// Constraints
	static constraints = {	 
		name(nullable:true)
		description(nullable:true)
		containerNumber(nullable:true)
		recipient(nullable:true)
		height(nullable:true)
		width(nullable:true)
		length(nullable:true)
		volumeUnits(nullable:true)
		weight(nullable:true)
		weightUnits(nullable:true)
		containerType(nullable:true)
		shipmentItems(nullable:true)		
		parentContainer(nullable:true)
		containerStatus(nullable:true)
		sortOrder(nullable:true)
	}
	
	int compareTo(obj) { name.compareTo(obj.name) }
	
	String getOptionValue() {
		return containerType.name + "-" + name
	}
	
}
