package org.pih.warehouse.order

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventCode;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;

class Order implements Serializable {
	
	String description 		// a user-defined, searchable name for the order 
	String orderNumber 		// an auto-generated shipment number
	Location origin			// the vendor
	Location destination 	// the customer location 
	Person recipient
	User orderedBy
	Date dateOrdered
	
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	static hasMany = [ orderItems : OrderItem, comments : Comment, documents : Document, events : Event ]
	static mapping = {
		table "`order`"
		orderItems cascade: "all-delete-orphan"
		comments cascade: "all-delete-orphan"
		documents cascade: "all-delete-orphan"
		events cascade: "all-delete-orphan"
	}
	
	static constraints = { 
		description(nullable:false, blank: false, maxSize: 255)
		orderNumber(nullable:true, maxSize: 255)
		origin(nullable:false)
		destination(nullable:false)
		recipient(nullable:true)
		orderedBy(nullable:false)
		dateOrdered(nullable:false)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}	
	
}