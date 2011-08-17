package org.pih.warehouse.order

import java.util.Date;
import java.util.Set;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.cart.Cart;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.receiving.ReceiptItem;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentException;
import org.pih.warehouse.shipping.ShipmentItem;

class OrderService {

	boolean transactional = true
	
	def shipmentService;

	List<Order> getIncomingOrders(Location location) { 
		return Order.findAllByDestination(location)
	}

	List<Order> getOutgoingOrders(Location location) { 
		return Order.findAllByOrigin(location)
	}
	
	List<Location> getSuppliers() { 
		def suppliers = []
		LocationType supplierType = LocationType.findById(Constants.SUPPLIER_LOCATION_TYPE_ID);
		if (supplierType) { 
			suppliers = Location.findAllByLocationType(supplierType);
		}
		return suppliers;
		
	}
	
	OrderCommand getOrder(Integer id, Integer recipientId) { 
		def orderCommand = new OrderCommand();
		
		def orderInstance = Order.get(id)
		if (!orderInstance)
			throw new Exception("Unable to locate order with ID " + id)
			
		if (recipientId) {
			orderCommand.recipient = Person.get(recipientId)
		}
		
		orderCommand.origin = Location.get(orderInstance?.origin?.id)
		orderCommand.destination = Location.get(orderInstance?.destination?.id)
		orderCommand.orderedBy = Person.get(orderInstance?.orderedBy?.id)
		orderCommand.dateOrdered = orderInstance?.dateOrdered
		orderCommand.order = orderInstance;
		orderInstance?.orderItems?.each {
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
		return orderCommand;
	}
	
		
	OrderCommand saveOrderShipment(OrderCommand orderCommand) { 
		def shipmentInstance = new Shipment()
		def shipments = orderCommand?.order?.shipments();
		def numberOfShipments = (shipments) ? shipments?.size() + 1 : 1;
		
		shipmentInstance.name = orderCommand?.order?.description + " - " + "Shipment #"  + numberOfShipments 
		shipmentInstance.shipmentType = orderCommand?.shipmentType;
		shipmentInstance.origin = orderCommand?.order?.origin;
		shipmentInstance.destination = orderCommand?.order?.destination;		
		shipmentInstance.expectedDeliveryDate = orderCommand?.deliveredOn;
		shipmentInstance.expectedShippingDate = orderCommand?.shippedOn;
		
		orderCommand?.shipment = shipmentInstance
		orderCommand?.orderItems.each { orderItemCommand ->
			
			// Ignores any null order items and makes sure that the order item has a product and quantity
			if (orderItemCommand && orderItemCommand.productReceived && orderItemCommand?.quantityReceived) {
				def shipmentItem = new ShipmentItem();
				shipmentItem.lotNumber = orderItemCommand.lotNumber
				shipmentItem.expirationDate = orderItemCommand.expirationDate
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
			shipmentService.saveShipment(shipmentInstance);
		}
		else { 
			log.info("Errors with shipment " + shipmentInstance?.errors)
			throw new ShipmentException(message: "Validation errors on shipment ", shipment: shipmentInstance)
		}
		
		
		
		// Send shipment, receive shipment, and add 
		if (shipmentInstance) { 
			// Send shipment 
			log.info "Sending shipment " + shipmentInstance?.name
			shipmentService.sendShipment(shipmentInstance, "", orderCommand?.currentUser, orderCommand?.currentLocation, orderCommand?.shippedOn, [] as Set);
						
			// Receive shipment
			log.info "Receiving shipment " + shipmentInstance?.name
			Receipt receiptInstance = shipmentService.createReceipt(shipmentInstance, orderCommand?.deliveredOn)
			
			// FIXME 
			// receiptInstance.validate() && !receiptInstance.hasErrors()
			if (!receiptInstance.hasErrors() && receiptInstance.save()) { 
				shipmentService.receiveShipment(shipmentInstance, "", orderCommand?.currentUser, orderCommand?.currentLocation);
			}
			else { 
				throw new ShipmentException(message: "Unable to save receipt ", shipment: shipmentInstance)
			}
			
			saveOrder(orderCommand?.order);
		}
		return orderCommand;
	}
	
	
	Order saveOrder(Order order) { 		
		// update the status of the order before saving
		order.updateStatus()
		
		if (!order.hasErrors() && order.save()) {
			return order;
		}
		else {
			throw new OrderException(message: "Unable to save order due to errors", order: order)
		}
	}
	
}
