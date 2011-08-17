package org.pih.warehouse.request

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventCode;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.fulfillment.Fulfillment;
import org.pih.warehouse.request.RequestStatus;

class Request implements Serializable {
	
	RequestStatus status;
	String description 		// a user-defined, searchable name for the order 
	String requestNumber 	// an auto-generated shipment number
	Location origin			// the vendor
	Location destination 	// the customer location 
	Person recipient
	Person requestedBy
	Date dateRequested
	Fulfillment fulfillment;
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	
	static hasMany = [ requestItems : RequestItem, comments : Comment, documents : Document, events : Event ]
	static mapping = {
		requestItems cascade: "all-delete-orphan", sort: "id"
		comments cascade: "all-delete-orphan"
		documents cascade: "all-delete-orphan"
		events cascade: "all-delete-orphan"
	}
	
	static constraints = { 
		status(nullable:true)
		description(nullable:false, blank: false, maxSize: 255)
		requestNumber(nullable:true, maxSize: 255)
		origin(nullable:false)
		destination(nullable:false)
		recipient(nullable:true)
		requestedBy(nullable:false)
		dateRequested(nullable:true)
		fulfillment(nullable:true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}	
	
	Boolean isPending() { 
		return isNotRequested() || isRequested();
	}
	
	Boolean isNotRequested() { 
		return (status == null || status == RequestStatus.NOT_REQUESTED)
	}
	
	Boolean isRequested() { 
		return (status in [RequestStatus.REQUESTED, RequestStatus.OPEN])
	}

	Boolean isFulfilled() { 
		return (status in [RequestStatus.FULFILLED, RequestStatus.SHIPPED, RequestStatus.RECEIVED])
	}
	
	Boolean isShipped() {
		return (status in [RequestStatus.SHIPPED, RequestStatus.RECEIVED])
	}

	Boolean isReceived() {
		return (status in [RequestStatus.RECEIVED])
	}

	Boolean isCanceled() {
		return (status in [RequestStatus.CANCELED])
	}

	String getRequestNumber() {
		return (id) ? "R" + String.valueOf(id).padLeft(6, "0")  : "(new request)";
	}
	
	
	
}