/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.request

import org.pih.warehouse.auth.AuthService;
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
	
	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		updatedBy = AuthService.currentUser.get()
	}
	
	String id
	String name
	String description 		// a user-defined, searchable name for the order 
	String requestNumber 	// an auto-generated reference number
	
	
	RequestStatus status;
	Location origin			// the vendor
	Location destination 	// the customer location 
	
	Person requestedBy
	Person recipient
	String recipientProgram	
	
	
	
	Date dateRequested
	Date dateValidFrom 
	Date dateValidTo
	
	Fulfillment fulfillment;
	
	// Audit fields
	Date dateCreated
	Date lastUpdated
	User createdBy
	User updatedBy
	
	static hasMany = [ requestItems : RequestItem, comments : Comment, documents : Document, events : Event ]
	static mapping = {
		id generator: 'uuid'
		requestItems cascade: "all-delete-orphan", sort: "id"
		comments cascade: "all-delete-orphan"
		documents cascade: "all-delete-orphan"
		events cascade: "all-delete-orphan"
	}
	
	static constraints = { 
		status(nullable:true)		
		name(nullable:true)
		description(nullable:true)
		requestNumber(nullable:true, maxSize: 255)
		origin(nullable:false)
		destination(nullable:false)
		recipient(nullable:true)
		requestedBy(nullable:false)
		dateRequested(nullable:true)
		fulfillment(nullable:true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
		dateValidFrom(nullable:true)
		dateValidTo(nullable:true)
		createdBy(nullable:true)
		updatedBy(nullable:true)
	}	
	
	Boolean isPending() { 
		return isNew() || isRequested();
	}
	
	Boolean isNew() { 
		return (status == null || status == RequestStatus.NEW)
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
		//return (id) ? "R" + String.valueOf(id).padLeft(6, "0")  : "";
		return id
	}
	
	
	
}