package org.pih.warehouse.shipping;

import org.pih.warehouse.user.User

class Comment {

	Date sendDate 
	String comment
	User commenter
	User recipient
	
	String toString() { return "$comment"; }
	
	static constraints = {
		comment(nullable:false)
		sendDate(nullable:false)		
		commenter(nullable:true)
		recipient(nullable:true)
	}
}
