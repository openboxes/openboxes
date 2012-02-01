package org.pih.warehouse.receiving

import java.io.Serializable;
import java.util.Date;

import org.pih.warehouse.core.Person;
import org.pih.warehouse.shipping.Shipment;

class Receipt implements Serializable {

	String id
	Date expectedDeliveryDate			 
	Date actualDeliveryDate				 
	Person recipient					
	Date dateCreated;
	Date lastUpdated;

	static belongsTo = [ shipment : Shipment ]
	static hasMany = [ receiptItems : ReceiptItem ]
	
	static mapping = {
		id generator: 'uuid'
	}

	// Constraints
	static constraints = {
		expectedDeliveryDate(nullable:true)
		actualDeliveryDate(blank:false, 
			// can't be delivered in the future
			// can't be delivered before it is shipped!
			validator: { value, obj-> 
				println "max: " + it + " <= " + new Date();				
				println "obj.shipment.actualShippingDate is notNull: " + obj.shipment.actualShippingDate 
				println "value + 1: " + (value + 1) 
				println "(value + 1).after(obj.shipment.actualShippingDate): " + (value + 1).after(obj.shipment.actualShippingDate) 
				if (!(value <= new Date())) {
					//println "value <= new Date(): " + (value <= new Date())
					return ["invalid.mustOccurOnOrBeforeToday", value, new Date()]
				}				
				if (!(value + 1).after(obj.shipment.actualShippingDate)) { 
					return ["invalid.mustOccurOnOrAfterActualShippingDate", value, obj.shipment.actualShippingDate]
				}
			}
		)
		recipient(nullable:true)
	}
	
}
