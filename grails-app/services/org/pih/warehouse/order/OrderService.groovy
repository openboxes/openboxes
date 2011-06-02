package org.pih.warehouse.order

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.order.cart.Cart;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class OrderService {

	boolean transactional = true
	

	List<Order> getIncomingOrders(Location location) { 
		return Order.findAllByDestination(location)
	}

	List<Order> getOutgoingOrders(Location location) { 
		return Order.findAllByOrigin(location)
	}
	
	
	OrderCommand getOrder(Integer id, Integer recipientId) { 
		def orderCommand = new OrderCommand();
		
		def orderInstance = Order.get(id)
		if (!orderInstance)
			throw new Exception("Unable to locate order with ID " + id)
			
		if (recipientId) 
			orderCommand.recipient = Person.get(recipientId)
		
		orderCommand.order = orderInstance;
		orderInstance?.orderItems?.each {
			if (!it?.isComplete()) {
				def orderItemCommand = new OrderItemCommand();
				orderItemCommand.primary = true;
				orderItemCommand.orderItem = it
				orderItemCommand.type = it.orderItemType
				orderItemCommand.description = it.description
				orderItemCommand.productReceived = it.product
				orderItemCommand.quantityOrdered = it.quantity;
				//orderItemCommand.quantityReceived = it.quantity
				orderCommand?.orderItems << orderItemCommand
			}
		}
		return orderCommand;
	}
	
		
	void saveOrderShipment(OrderCommand orderCommand) { 
		def shipmentInstance = new Shipment()
		def numberOfShipments = orderCommand?.order?.shipments()?.size() + 1
		log.info("shipments " + orderCommand?.order?.shipments())
		log.info("number of shipments " + numberOfShipments )
		shipmentInstance.name = "Shipment #"  + numberOfShipments + " - " + orderCommand?.order?.description
		shipmentInstance.shipmentType = orderCommand?.shipmentType;
		shipmentInstance.origin = orderCommand?.order?.origin;
		shipmentInstance.destination = orderCommand?.order?.destination;		
		shipmentInstance.expectedDeliveryDate = orderCommand?.deliveredOn;
		shipmentInstance.expectedShippingDate = orderCommand?.shippedOn;
		
		orderCommand?.orderItems.each { orderItemCommand ->
			if (orderItemCommand.productReceived && orderItemCommand?.quantityReceived) {
				def shipmentItem = new ShipmentItem();
				shipmentItem.lotNumber = orderItemCommand.lotNumber
				shipmentItem.product = orderItemCommand.productReceived;
				shipmentItem.quantity = orderItemCommand.quantityReceived;
				shipmentItem.recipient = orderCommand?.recipient;
				shipmentInstance.addToShipmentItems(shipmentItem)
				
				def orderShipment = new OrderShipment(shipmentItem:shipmentItem, orderItem:orderItemCommand?.orderItem)
				shipmentItem.addToOrderShipments(orderShipment)
				orderItemCommand?.orderItem.addToOrderShipments(orderShipment)
			}
		}
		
		// Validate the shipment and save it if there are no errors
		if (shipmentInstance.validate() && !shipmentInstance.hasErrors()) { 
			log.info("No errors, save shipment");
			shipmentInstance.save(flush:true)		
				
		}
		else { 
			log.info("Errors with shipment " + shipmentInstance?.errors)
		}
		orderCommand?.shipment = shipmentInstance
	}
	
	
	boolean saveOrder(Order order) { 
		
		log.info order?.orderItems
		
		if (order.validate() && !order.hasErrors()) {
			log.info("save order")
			if (!order.hasErrors() && order.save()) {
				// no errors and saved
			}
			else {
				log.info("error during save")
				return false;
			}
		}
		else {
			log.info("error during validation")
			return false;
		}
		return true;
	}
	
	
	void createOrderFromCart(Cart cart) { 		
		
	}
	
	
	
}
