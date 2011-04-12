package org.pih.warehouse.core;

import java.util.Date;
import org.pih.warehouse.core.User

class Comment implements Serializable {

	String comment
	User sender
	User recipient
	Date dateSent
	Date dateRead
	Date dateCreated;
	Date lastUpdated;

	// Comments should exist on their own in case we want to tie them to other objects. 
	// Shipment (and other entities) should create a join table for comments.
	//static belongsTo = [ shipment : Shipment ];
	
	
	static constraints = {
		comment(nullable:false, maxSize: 255)
		sender(nullable:true)
		recipient(nullable:true)
		dateSent(nullable:true, min: new Date())		
		dateRead(nullable:true)		
	}

	String toString() { return "$comment"; }
}
