package org.pih.warehouse.receiving

import java.util.Date;

import org.pih.warehouse.core.Person;
import org.pih.warehouse.shipping.Shipment;

class Receipt {

	Shipment shipment					// Shipment that is being received
	Date expectedDeliveryDate			 
	Date actualDeliveryDate				 
	Person recipient					
	Date dateCreated;
	Date lastUpdated;
	
	static hasMany = [ receiptItems : ReceiptItem ]
	
	
	// Constraints
	static constraints = {
		shipment(nullable:false)
		expectedDeliveryDate(nullable:true)
		actualDeliveryDate(nullable:true)
		recipient(nullable:true)
	}
	
}
