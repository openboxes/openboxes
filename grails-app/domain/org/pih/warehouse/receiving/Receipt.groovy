package org.pih.warehouse.receiving

import java.io.Serializable;
import java.util.Date;

import org.pih.warehouse.core.Person;
import org.pih.warehouse.shipping.Shipment;

class Receipt implements Serializable {

	Date expectedDeliveryDate			 
	Date actualDeliveryDate				 
	Person recipient					
	Date dateCreated;
	Date lastUpdated;
	
	static belongsTo = [ shipment : Shipment ]
	static hasMany = [ receiptItems : ReceiptItem ]
	
	
	// Constraints
	static constraints = {
		expectedDeliveryDate(nullable:true)
		actualDeliveryDate(blank:false, 
			// can't be delivered in the future
			max: new Date(),  
			// can't be delivered before it is shipped!
			validator: { value, obj-> 
				println "obj.shipment.actualShippingDate = " + obj.shipment.actualShippingDate 
				println "value + 1 = " + (value + 1) 
				println "(value + 1).after(obj.shipment.actualShippingDate) = " + (value + 1).after(obj.shipment.actualShippingDate) 
				obj.shipment.actualShippingDate && (value + 1).after(obj.shipment.actualShippingDate)})	   
		
		recipient(nullable:true)
	}
	
}
