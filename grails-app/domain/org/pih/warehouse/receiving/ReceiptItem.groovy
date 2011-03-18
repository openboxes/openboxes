package org.pih.warehouse.receiving

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.product.Product;

class ReceiptItem {
	
	Product product		    			// Specific product that we're tracking
	String lotNumber					// Loose coupling to the inventory lot
	String serialNumber					// Serial number of a particular product (optional)
	Date expirationDate					// Date of expiration

	Integer quantityShipped				// Quantity that was shipped
	Integer quantityReceived			// Quantity could be a class on its own
		
	Person recipient 					// Recipient of an item	
	String comment 						// Comment about the item quality 
	
	Date dateCreated;
	Date lastUpdated;
	
	static belongsTo = [ receipt : Receipt ]
	static constraints = {
		product(nullable:false)
		lotNumber(nullable:true)
		serialNumber(nullable:true)
		expirationDate(nullable:true)
		quantityShipped(min:0, nullable:false)
		quantityReceived(min:0, nullable:false)		
		recipient(nullable:true)
		comment(nullable:true)
	}
	
	int compareTo(obj) { product.name.compareTo(obj.product.name) }
	
}
