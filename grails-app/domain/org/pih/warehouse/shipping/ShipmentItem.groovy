/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping

import org.pih.warehouse.core.Person
import org.pih.warehouse.donation.Donor
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.OrderShipment
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.ReceiptItem

// import java.util.Date

class ShipmentItem implements Comparable, Serializable {

	//def beforeDelete = {
	//	container.removeFromShipmentItems(this)
	//}

	String id
	//String description		// Item description (for one-off items)
	String lotNumber			// Loose coupling to the inventory lot 
	Date expirationDate			
	Product product		    	// Specific product that we're tracking
	Integer quantity		    // Quantity could be a class on its own				
	Person recipient 			// Recipient of an item
	Donor donor					// Organization that donated the goods
	Date dateCreated;
	Date lastUpdated;
	InventoryItem inventoryItem
	Container container				// 
	//PackageType packageType		// The type of packaging that this item is stored 
									// within.  This is different from the container type  
									// (which might be a pallet or shipping container), in  
									// that this will likely be a box that the item is 
									// actually contained within.
	
	static belongsTo = [ shipment : Shipment ]
	
	static hasMany = [ orderShipments : OrderShipment ]
	
	static hasOne = [receiptItem: ReceiptItem]
	
	
	static mapping = {
		id generator: 'uuid'
		cache true
	}
	
	//static belongsTo = [ container : Container ] // + shipment : Shipment
	static constraints = {
		container(nullable:true)
		product(nullable:false)  // TODO: this doesn't seem to prevent the product field from being empty
		lotNumber(nullable:true, maxSize: 255)
		expirationDate(nullable:true)
		quantity(min:0, range: 0..2147483646)
		recipient(nullable:true)
		inventoryItem(nullable:true)
		receiptItem(nullable:true)
		donor(nullable:true)
	}
    
	/**
	 * @return	the lot number of the inventory item (or the lot number of the shipment item for backwards compatibility)
	 */
	def getLotNumber() { 
		return inventoryItem?.lotNumber?:lotNumber
	}
	
	/**
	 * @return	the expiration date of the inventory item (or the expiration date of the shipment item for backwards compatibility)
	 */
	def getExpirationDate() { 
		return inventoryItem?.expirationDate?:expirationDate
	}
	

	def listOrderItems() {
		return orderShipments.collect{it.orderItem}
	}

	
	def totalQuantityShipped() {
		int totalQuantityShipped = 0
		// Should use inventory item instead of comparing product & lot number
		if (shipment.shipmentItems) {
			shipment.shipmentItems.each {
				if (it.product == this.product && it.lotNumber == this.lotNumber) {
					totalQuantityShipped += it.quantity
				}
			}
		}
		return totalQuantityShipped
	}

	def totalQuantityReceived() {
		int totalQuantityReceived = 0
		// Should use inventory item instead of comparing product & lot number
		if (shipment.receipt) { 
			shipment.receipt.receiptItems.each {
				if (it.product == this.product && it.lotNumber == this.lotNumber) {
					totalQuantityReceived += it.quantityReceived
				}
			}
		}
		return totalQuantityReceived
	}
	
	/*
	List addToOrderShipments(OrderShipment orderShipment) {
		OrderShipment.link(orderShipment, this)
		return orderShipments()
	}

	List removeFromOrderShipments(OrderShipment orderShipment) {
		OrderShipment.unlink(orderShipment, this)
		return orderShipments()
	}
	*/

    def receiptItems() {
        return ReceiptItem.findAllByShipmentItem(this)
    }

    def quantityReceived() {
        def receiptItems = receiptItems()
        return (receiptItems) ? receiptItems.sum { it.quantityReceived } : 0
    }

    /**
	 * Sorts shipping items by associated product name, then lot number, then quantity,
	 * and finally by id. 
	 * 
	 * FIXME Need to get rid of the product and lot number comparison
	 */
	int compareTo(obj) { 
		def sortOrder = 
			container?.sortOrder <=> obj?.container?.sortOrder ?:
				inventoryItem?.product?.name <=> obj?.inventoryItem?.product?.name ?:
					inventoryItem?.lotNumber <=> obj?.inventoryItem?.lotNumber ?:
						product?.name <=> obj?.product?.name ?: 
							lotNumber <=> obj?.lotNumber ?:
								quantity <=> obj?.quantity ?:
									id <=> obj?.id
		return sortOrder;
		/*
		if (!product?.name && obj?.product?.name) {
			return -1
		}
		else if (!obj?.product?.name && product?.name) {
			return 1
		}
		else {
			if (product?.name <=> obj?.product?.name != 0) {
				return product.name <=> obj.product.name
			}
			else {
				if (!lotNumber && obj?.lotNumber) {
					return -1
				}
				else if (!obj.lotNumber && lotNumber) {
					return 1
				}
				else if (lotNumber <=> obj?.lotNumber != 0) {
					return lotNumber <=> obj.lotNumber
				}
				else {
					if (!quantity && obj?.quantity) {
						return -1
					}
					else if (!obj.quantity && quantity) {
						return 1
					}
					else if (quantity <=> obj?.quantity != 0) {
						return quantity <=> obj.quantity
					}
					else {
						return id <=> obj.id
					}
				}
			}
		}
		*/
	}
	
	ShipmentItem cloneShipmentItem() {
		return new ShipmentItem(
			lotNumber: this.lotNumber, 
			expirationDate: this.expirationDate,			
			product: this.product,
			inventoryItem: this.inventoryItem,
			quantity: this.quantity,				
			recipient: this.recipient,
			donor: this.donor,
			container: this.container
		)
	}
}
