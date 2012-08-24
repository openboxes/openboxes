package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.order.Order;
import org.pih.warehouse.request.Request;
import org.pih.warehouse.shipping.Shipment;

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location implements Comparable, java.io.Serializable {
	
	String id
	String name
	byte [] logo				// logo
	Address address
	String fgColor	= "000000"
	String bgColor = "FFFFFF"
	
	Location parentLocation; 
	LocationType locationType	
	LocationGroup locationGroup;

	User manager								// the person in charge of the warehouse
	Inventory inventory							// each warehouse has a single inventory
	Boolean local = Boolean.TRUE				// indicates whether this warehouse is being managed on the locally deployed system
	Boolean active = Boolean.TRUE				// indicates whether this warehouse is currently active

	Date dateCreated;
	Date lastUpdated;

	
	static belongsTo = [ parentLocation : Location ]
	static hasMany = [ locations : Location, supportedActivities : String, employees: User  ]
		
	static constraints = {
		name(nullable:false, blank: false, maxSize: 255)
		address(nullable:true)
		locationType(nullable:false)
		locationGroup(nullable:true)
		parentLocation(nullable:true)
		bgColor(nullable:true, validator: {bgColor, obj ->
			def fgColor = obj.properties['fgColor']
			if(fgColor == null) return true 
			bgColor != fgColor ? true : ['invalid.matchingcolor']
		})
		fgColor(nullable:true)
		logo(nullable:true, maxSize:10485760) // 10 MBs
		manager(nullable:true)
		inventory(nullable:true)
		active(nullable:false)
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	
	static mapping = {
		id generator: 'uuid'
		// Needs to be eagerly fetched because of Location.supportsActivity() method
		supportedActivities lazy: false
		locationType lazy: false
	}
	
	static transients = ["transactions", "events", "shipments", "requests", "orders" ]
	
	List getTransactions() { return Transaction.findAllByDestinationOrSource(this,this) }
	List getEvents() { return Event.findAllByEventLocation(this) }
	List getShipments() { return Shipment.findAllByOriginOrDestination(this,this) }
	List getRequests() { return Request.findAllByOriginOrDestination(this,this) }
	List getOrders() { return Order.findAllByOriginOrDestination(this,this) } 
	List getUsers() { return User.findAllByWarehouse(this) }
	
	String toString() { return this.name } 
	
	int compareTo(obj) { 
		return name <=> obj?.name
	}
	
	/**
	 * Indicates whether the location supports the given activity.
	 * 
	 * @param activity	the given activity
	 * @return	true if the activity is supported, false otherwise
	 */
	Boolean supports(ActivityCode activity) {
		return supports(activity.id)
	}

	/**
	 * Indicates whether the location supports the given activity.
	 * 
	 * @param activity	the given activity id
	 * @return	true if the activity is supported, false otherwise
	 */
	Boolean supports(String activity) { 
		boolean supportsActivity = false
		if (supportedActivities) {
			supportsActivity = supportedActivities?.contains(activity);
		}
		else {
			supportsActivity = locationType?.supports(activity)
		}
		return supportsActivity;

	}
	
	Boolean isWarehouse() {
		//return locationType.id == LocationType.findById(Constants.WAREHOUSE_LOCATION_TYPE_ID).id
		return supports(ActivityCode.MANAGE_INVENTORY)
	}
}
