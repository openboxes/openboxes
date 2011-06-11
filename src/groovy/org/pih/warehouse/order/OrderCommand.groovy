package org.pih.warehouse.order

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.ShipmentType;

class OrderCommand implements Serializable {

	Order order
	Person recipient
	Date shippedOn
	Date deliveredOn
	ShipmentType shipmentType
	Shipment shipment
	
	User currentUser;
	Location currentLocation
	
	Location origin
	Location destination
	Date dateOrdered;
	Person orderedBy
	
	
	// Not the actual order items, but rather all the line items on the receive order page.  
	// This means that we might have more than one OrderItemCommand per OrderItem.
	def orderItems = 
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(OrderItemCommand.class));
	
	static constraints = {
		shipmentType(nullable:false)
		recipient(nullable:false)
		shippedOn(nullable:false)
		deliveredOn(nullable:false)
		currentUser(nullable:true)
		currentLocation(nullable:true)
		origin(nullable:true)
		destination(nullable:true)
		dateOrdered(nullable:true)
		orderedBy(nullable:true)
	}
	
}

