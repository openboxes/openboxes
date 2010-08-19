package org.pih.warehouse.shipping;

import java.util.Date;
import org.pih.warehouse.core.User



class Comment {

	String comment
	User sender
	User recipient
	Date dateSent
	Date dateRead
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;

	static belongsTo = [ shipment : Shipment ];
	
	
	static constraints = {
		comment(nullable:false)
		dateSent(nullable:false)		
		dateRead(nullable:false)		
		sender(nullable:true)
		recipient(nullable:true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}

	String toString() { return "$comment"; }
}
