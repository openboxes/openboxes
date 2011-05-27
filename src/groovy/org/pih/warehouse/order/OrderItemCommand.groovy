package org.pih.warehouse.order

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class OrderItemCommand {

	OrderItem orderItem
	ShipmentItem shipmentItem
	
	// from order item
	String type
	String description
	Integer quantityOrdered
	
	// for shipment item
	String lotNumber
	Product productReceived
	Integer quantityReceived	
		
	static constraints = {

	}
	
}

