package org.pih.warehouse.order

import java.util.Date;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ShipmentItem;

class OrderShipment implements Serializable {

	String id
	
	static mapping = {
		id generator: 'uuid'
	}

	//OrderItem orderItem
	//ShipmentItem shipmentItem
	static belongsTo = [shipmentItem: ShipmentItem, orderItem: OrderItem]
	
	/*
	static OrderShipment link(orderItem, shipmentItem) {
		def orderShipment = OrderShipment.findByOrderItemAndShipmentItem(orderItem, shipmentItem)
		if (!orderShipment) {
			orderShipment = new OrderShipment()
			orderShipment.orderItem = orderItem;
			orderShipment.shipmentItem = shipmentItem;			
			orderShipment.save()
		}
		return orderShipment
	}

	static void unlink(orderItem, shipmentItem) {
		def orderShipment = OrderShipment.findByOrderItemAndShipmentItem(orderItem, shipmentItem)
		if (orderShipment) {
			//orderItem?.removeFromOrderShipments(orderShipment)
			//shipmentItem?.removeFromOrderShipments(orderShipment)
			orderShipment.delete()
		}
	}	
	*/
}
