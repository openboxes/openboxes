package org.pih.warehouse.shipping

import java.util.Date;

import org.pih.warehouse.order.OrderShipment;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.donation.Donor;

class ShipmentItem implements Comparable, java.io.Serializable {

	//def beforeDelete = {
	//	container.removeFromShipmentItems(this)
	//}

	
	String lotNumber			// Loose coupling to the inventory lot 
	Product product		    	// Specific product that we're tracking
	Integer quantity		    // Quantity could be a class on its own				
	Person recipient 			// Recipient of an item
	Donor donor					// Organization that donated the goods
	Date dateCreated;
	Date lastUpdated;
			
	Container container				// 
	//PackageType packageType		// The type of packaging that this item is stored 
									// within.  This is different from the container type  
									// (which might be a pallet or shipping container), in  
									// that this will likely be a box that the item is 
									// actually contained within.
	
	static belongsTo = [ shipment : Shipment ]
	
	static hasMany = [ orderShipments : OrderShipment ]
	
	//static belongsTo = [ container : Container ] // + shipment : Shipment
	static constraints = {
		container(nullable:true)
		product(blank:false, nullable:false)  // TODO: this doesn't seem to prevent the product field from being empty
		lotNumber(nullable:true, maxSize: 255)
		quantity(min:0, blank:false, range: 0..2147483646)
		recipient(nullable:true)
		donor(nullable:true)
	}
    

	def orderItems() {
		return orderShipments.collect{it.orderItem}
	}

	def quantityReceived() {
		int ret = 0
		shipment.receipt.receiptItems.each {
			if (it.product == this.product && it.lotNumber == this.lotNumber) {
				ret += it.quantityReceived
			}
		}
		return ret
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
	
	
	/**
	 * Sorts shipping items by associated product name, then lot number, then quantity,
	 * and finally by id. 
	 */
	int compareTo(obj) { 
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
	}
	
	ShipmentItem cloneShipmentItem() {
		return new ShipmentItem(
			lotNumber: this.lotNumber, 
			product: this.product,
			quantity: this.quantity,				
			recipient: this.recipient,
			donor: this.donor,
			container: this.container
		)
	}
}
