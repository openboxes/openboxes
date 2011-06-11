package org.pih.warehouse.order

import java.util.Date;
import java.util.Set;

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.order.cart.Cart;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.receiving.ReceiptItem;
import org.pih.warehouse.shipping.Shipment;
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
		LocationType supplierType = LocationType.findByName("Supplier");
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
			
		if (recipientId) 
			orderCommand.recipient = Person.get(recipientId)
		
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
	
		
	void saveOrderShipment(OrderCommand orderCommand) { 
		def shipmentInstance = new Shipment()
		def numberOfShipments = orderCommand?.order?.shipments()?.size() + 1
		
		shipmentInstance.name = orderCommand?.order?.description + " - " + "Shipment #"  + numberOfShipments 
		shipmentInstance.shipmentType = orderCommand?.shipmentType;
		shipmentInstance.origin = orderCommand?.order?.origin;
		shipmentInstance.destination = orderCommand?.order?.destination;		
		shipmentInstance.expectedDeliveryDate = orderCommand?.deliveredOn;
		shipmentInstance.expectedShippingDate = orderCommand?.shippedOn;
		
		orderCommand?.orderItems.each { orderItemCommand ->
			if (orderItemCommand && orderItemCommand.productReceived && orderItemCommand?.quantityReceived) {
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
			//shipmentInstance.save(flush:true)		
			shipmentService.saveShipment(shipmentInstance);
		}
		else { 
			log.info("Errors with shipment " + shipmentInstance?.errors)
		}
		
		if (shipmentInstance) { 
			// Send shipment 
			log.info "Sending shipment " + shipmentInstance?.name
			shipmentService.sendShipment(shipmentInstance, "", orderCommand?.currentUser, orderCommand?.currentLocation, orderCommand?.shippedOn, [] as Set);
						
			// Receive shipment
			log.info "Receiving shipment " + shipmentInstance?.name
			Receipt receiptInstance = new Receipt()
			
			shipmentInstance.receipt = receiptInstance
			receiptInstance.shipment = shipmentInstance	
			
			receiptInstance.recipient = shipmentInstance?.recipient	
			receiptInstance.expectedDeliveryDate = shipmentInstance?.expectedDeliveryDate;
			receiptInstance.actualDeliveryDate = orderCommand?.deliveredOn;
			shipmentInstance.shipmentItems.each {
				log.info("Adding shipment item as receipt item" + it.quantity)
				ReceiptItem receiptItem = new ReceiptItem(it.properties);
				receiptItem.setQuantityShipped (it.quantity);
				receiptItem.setQuantityReceived (it.quantity);
				receiptItem.setLotNumber(it.lotNumber);
				receiptInstance.addToReceiptItems(receiptItem);           // use basic "add" method to avoid GORM because we don't want to persist yet
			}
			if (!receiptInstance.hasErrors() && receiptInstance.save(flush:true)) { 
				shipmentService.receiveShipment(shipmentInstance, receiptInstance, "", orderCommand?.currentUser, orderCommand?.currentLocation);
			}
			else { 
				throw new RuntimeException("Unable to save receipt " + receiptInstance.errors)
			}
			
		}
		
		orderCommand?.shipment = shipmentInstance
	}
	
	
	boolean saveOrder(Order order) { 
		
		log.info order?.orderItems
		
		if (order.validate() && !order.hasErrors()) {
			log.info("save order")
			if (!order.hasErrors() && order.save()) {
				log.info("no errors, saved " + order.id)
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
