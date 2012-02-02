package org.pih.warehouse.receiving

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ShipmentItem;

class ReceiptItem implements Serializable {
	
	String id
	Product product		    			// Specific product that we're tracking
	String lotNumber					// Loose coupling to the inventory lot
	Date expirationDate					// Date of expiration

	Integer quantityShipped				// Quantity that was shipped
	Integer quantityReceived			// Quantity could be a class on its own
	
	ShipmentItem shipmentItem
	InventoryItem inventoryItem
		
	Person recipient 					// Recipient of an item	
	String comment 						// Comment about the item quality 
	
	Date dateCreated;
	Date lastUpdated;
	
	static mapping = {
		id generator: 'uuid'
	}
	
	static belongsTo = [ receipt : Receipt ]
	static constraints = {
		product(nullable:false)
		lotNumber(nullable:true, maxSize: 255)
		expirationDate(nullable:true)
		shipmentItem(nullable:true)
		inventoryItem(nullable:true)
		quantityShipped(range: 0..2147483646, nullable:false)
		quantityReceived(range: 0..2147483646, nullable:false)		
		recipient(nullable:true)
		comment(nullable:true, maxSize: 255)
	}
	
	int compareTo(obj) { product.name.compareTo(obj.product.name) }
	
}
