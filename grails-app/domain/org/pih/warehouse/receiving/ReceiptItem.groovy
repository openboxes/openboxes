package org.pih.warehouse.receiving

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.product.Product;

class ReceiptItem implements Serializable {
	
	Product product		    			// Specific product that we're tracking
	String lotNumber					// Loose coupling to the inventory lot
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
		lotNumber(nullable:true, maxSize: 255)
		expirationDate(nullable:true)
		quantityShipped(range: 0..2147483646, nullable:false)
		quantityReceived(range: 0..2147483646, nullable:false)		
		recipient(nullable:true)
		comment(nullable:true, maxSize: 255)
	}
	
	int compareTo(obj) { product.name.compareTo(obj.product.name) }
	
}
