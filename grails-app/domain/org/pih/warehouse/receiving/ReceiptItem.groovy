/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.receiving

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ShipmentItem;

class ReceiptItem implements Comparable, Serializable {
	
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
	
	//int compareTo(obj) { product.name.compareTo(obj.product.name) }
	
	/**
	* Sorts receipt items in the same order as shipment items.
	*/
   int compareTo(obj) {
	   return shipmentItem <=> obj.shipmentItem
	}
	
}
