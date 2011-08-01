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
	
	OrderStatus status;
	String description 		// a user-defined, searchable name for the order 
	String orderNumber 		// an auto-generated shipment number
	Location origin			// the vendor
	Location destination 	// the customer location 
	Person recipient
	Person orderedBy
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
		status(nullable:true)
		description(nullable:false, blank: false, maxSize: 255)
		orderNumber(nullable:true, maxSize: 255)
		origin(nullable:false)
		destination(nullable:false)
		recipient(nullable:true)
		orderedBy(nullable:false)
		dateOrdered(nullable:true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}	
	
	String status() { 
		if (isPlaced() && isPartiallyReceived()) {
			return "Partially Received"
		}
		else if (isPlaced()) { 
			return "Placed"
		}
		else if (isReceived()) { 
			return "Received"
		} 
		return "Pending"
	}
	
	/**
	 * @return	a boolean indicating whether the order is pending
	 */
	Boolean isPending() { 
		return (status == null || status == OrderStatus.PENDING )
	}
	
	/**
	 * @return	a boolean indicating whether the order has been placed
	 */
	Boolean isPlaced() { 
		return (status == OrderStatus.PLACED)
	}
	
	/**
	 * @return	a boolean indicating whether the order has been received
	 */
	Boolean isReceived() { 
		return (status == OrderStatus.RECEIVED)
	}
	
	
	/**
	 * After an order is placed and before it is completed received, the order can 
	 * be partially received.  This occurs when the order contains items that have 
	 * been completely received and some that have not been completely received.
	 * 
	 * @return
	 */
	Boolean isPartiallyReceived() { 
		return orderItems?.find { it.isPartiallyFulfilled() } 
	}

	/**
	 * After an order has been placed, it will be in a state where it can be received.
	 * 
	 * @return	a boolean value indicating whether all items have been received entirely
	 */
	Boolean isCompletelyReceived() {
		return orderItems?.size() == orderItems?.find { it.isCompletelyFulfilled() }?.size()
	}
	
	String getOrderNumber() {
		return (id) ? "V" + String.valueOf(id).padLeft(6, "0")  : "(New Request)";
	}
	
	def shipments() { 
		return orderItems.collect { it.shipments() }.flatten().unique() { it?.id }
	}
	
	def totalPrice() { 
		return orderItems.collect { it.totalPrice() }.sum();
	}
	
}