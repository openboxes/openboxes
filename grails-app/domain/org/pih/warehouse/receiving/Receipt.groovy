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
		actualDeliveryDate(bank:false, 
			validator: { value, obj-> obj.shipment.actualShippingDate && (value + 1).after(obj.shipment.actualShippingDate) })	   // can't be delivered before it is shipped!
		recipient(nullable:true)
	}
	
}
