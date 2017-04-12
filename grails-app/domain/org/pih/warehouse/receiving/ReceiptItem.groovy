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

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

// import java.util.Date

class ReceiptItem implements Comparable, Serializable {
	
	String id
	Product product		    			// Specific product that we're tracking
	String lotNumber					// Loose coupling to the inventory lot
	Date expirationDate					// Date of expiration

	Integer quantityShipped				// Quantity that was shipped
	Integer quantityReceived			// Quantity could be a class on its own
	
	//Integer quantityAccepted
	//Integer quantityRejected
	//String rejectionReason
	//String description				// Item description (for one-off items)
	String comment 						// Comment about the item quality
	
	ShipmentItem shipmentItem
	InventoryItem inventoryItem
	Location binLocation
		
	Person recipient 					// Recipient of an item	
	
	Date dateCreated;
	Date lastUpdated;
	
	static mapping = {
		id generator: 'uuid', sqlType: "char(38)"
	}
	
	static belongsTo = [ receipt : Receipt ]
	static constraints = {
		product(nullable:false)
		lotNumber(nullable:true, maxSize: 255)
		expirationDate(nullable:true)
		shipmentItem(nullable:true)
		inventoryItem(nullable:true)
		binLocation(nullable:true)
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

    String toString() {
        return "${id}:${product.name}:${inventoryItem.lotNumber}:${quantityShipped}:${quantityReceived}:${comment}"
    }

	
}
