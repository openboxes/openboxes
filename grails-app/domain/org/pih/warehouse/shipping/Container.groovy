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
	//SortedSet containers				// Child containers (in combination with mapping, helps to order containers)

	//static belongsTo = [ parentContainer : Container ]
	static belongsTo = [ shipment : Shipment ];
	static hasMany = [ containers : Container];
	//static mappedBy = [containers: 'parentContainer']

	static transients = [ "optionValue", "shipmentItems" ]
	static mapping = {
		containers cascade: "all-delete-orphan"
	}
		
	// Constraints
	static constraints = {	 
		name(empty:false)
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
	
	String toString() { name } 

	int compareTo(obj) { 
		if (!sortOrder && obj?.sortOrder) {
			return -1
		}
		else if (!obj?.sortOrder && sortOrder) {
			return 1
		}
		else if (sortOrder <=> obj?.sortOrder != 0) {
			return sortOrder <=> obj?.sortOrder
		}
		else {
			return id <=> obj?.id
		}
	}
	
	/**
	 * Makes a copy of this container
	 * But does not copy references to associated shipments or child containers
	 * Also doesn't copy id, date created and last updated
	 */
	Container copyContainer() {

		// TODO: figure out sort order!

		Container newContainer = new Container (
			name: this.name,
			containerNumber: this.containerNumber,
			description: this.description,
			recipient: this.recipient,
			sortOrder: this.sortOrder,
			height: this.height,
			width: this.width,
			length: this.length,
			volumeUnits: this.volumeUnits,
			weight: this.weight,
			weightUnits: this.weight,
			containerType: this.containerType,
			containerStatus: this.containerStatus
		)
	
	}
	
	List<ShipmentItem> getShipmentItems() { 
		return ShipmentItem.findAllByContainer(this)
	}
	
	String getOptionValue() {
		return containerType.name + "-" + name
	}
	
	/**
	 * Adds a new container to this container of the specified type
	 */
	Container addNewContainer (ContainerType containerType) {
		def sortOrder = (this.containers) ? this.containers.size()+1 : 1
		
		def container = new Container(
			containerType: containerType, 
			shipment: this,
			sortOrder: sortOrder
		)
		
		this.addToContainers(container)
		this.shipment.addToContainers(container)
		
		return container
	}
	
	
	/**
	 * Adds a new item to the container
	 */
	ShipmentItem addNewItem () {
		
		def item = new ShipmentItem(
			container: this, 
			shipment: this.shipment
		)
		
		this.shipment.addToShipmentItems(item)
		
		return item
	}
}
