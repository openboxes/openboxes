package org.pih.warehouse.shipping

import java.io.Serializable;
import java.util.Date;

import org.pih.warehouse.inventory.Transaction;

class SendShipmentCommand implements Serializable { 
	
	String comments	
	Shipment shipment 
	Transaction transaction
	ShipmentWorkflow shipmentWorkflow
	Boolean debitStockOnSend = true
	Date actualShippingDate	
	
	static constraints = { 
		comments(nullable:true)
		transaction(nullable:true)
		shipment(nullable:false, validator: { value, obj -> !obj.shipment.hasShipped() })
		shipmentWorkflow(nullable:true)
		debitStockOnSend(nullable:false)
		actualShippingDate(nullable:false) //validator: { value, obj-> value > new Date()}
	}	
	
}
