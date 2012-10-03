/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.order

import java.util.Date
import java.util.List
import java.util.Map
import java.util.Set

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.ListCommand;
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentException
import org.pih.warehouse.shipping.ShipmentItem

class OrderService {

	boolean transactional = true
	
	def shipmentService;
	
	List<Order> getOrdersPlacedByLocation(Location orderPlacedBy, Location orderPlacedWith, OrderStatus status, Date orderedFromDate, Date orderedToDate) {
		def orders = Order.withCriteria {
			and {
				eq("destination", orderPlacedBy)
				if (orderPlacedWith) { eq("origin", orderPlacedWith) }
				if (status) { eq("status", status) }
				if (orderedFromDate) { ge("dateOrdered", orderedFromDate) }
				if (orderedToDate) { le("dateOrdered", orderedToDate) }
			}
		}
		return orders
   }

	/**
	 * @param location
	 * @return	a list of pending incoming order into the given location
	 */
	List<Order> getIncomingOrders(Location location) { 
		return Order.findAllByDestination(location);//.findAll { it.isPending() }
	}

	
	/**
	 * @param location
	 * @return	a list of pending outgoing order from the given location
	 */
	List<Order> getOutgoingOrders(Location location) { 
		return Order.findAllByOrigin(location);//.findAll { it.isPending() }
	}
	
	/**
	 * @return	a list of suppliers
	 */
	List<Location> getSuppliers() { 
		def suppliers = []
		LocationType supplierType = LocationType.findById(Constants.SUPPLIER_LOCATION_TYPE_ID);
		if (supplierType) { 
			suppliers = Location.findAllByLocationType(supplierType);
		}
		return suppliers;
		
	}
	
	/**
	 * @param id	an identifier for the order
	 * @param recipientId
	 * @return	an command object based on an order with the given  
	 */
	OrderCommand getOrder(String id, Integer recipientId) { 
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
	
	/**
	 * 
	 * @param orderCommand
	 * @return
	 */
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
			shipmentService.sendShipment(shipmentInstance, "", orderCommand?.currentUser, orderCommand?.currentLocation, orderCommand?.shippedOn);
						
			// Receive shipment
			log.info "Receiving shipment " + shipmentInstance?.name
			Receipt receiptInstance = shipmentService.createReceipt(shipmentInstance, orderCommand?.deliveredOn)
			
			// FIXME 
			// receiptInstance.validate() && !receiptInstance.hasErrors()
			if (!receiptInstance.hasErrors() && receiptInstance.save()) { 
				shipmentService.receiveShipment(shipmentInstance, "", orderCommand?.currentUser, orderCommand?.currentLocation, true);
			}
			else { 
				throw new ShipmentException(message: "Unable to save receipt ", shipment: shipmentInstance)
			}
			
			saveOrder(orderCommand?.order);
		}
		return orderCommand;
	}
	
	/**
	 * 
	 * @param order
	 * @return
	 */	
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
	
	/**
	 *
	 * @param location
	 * @return
	 */
	List<Order> getPendingOrders(Location location) {
		def orders = Order.withCriteria { 
			or { 
				eq("origin", location) 
				eq("destination", location) 
			}
		}			
		return orders; //.findAll { it.isPending() }
	}

	/**
	 *
	 * @param location
	 * @param product
	 * @return
	 */
	List<OrderItem> getPendingOrderItemsWithProduct(Location location, Product product) {
		def orderItems = []
		def orders = getPendingOrders(location);
		orders.each {
			def orderItemList = it.orderItems.findAll { it.product == product }
			orderItemList.each { orderItems << it; }
		}

		return orderItems;
	}
	
	
	/**
	 *
	 * @param location
	 * @return
	 */
	Map getIncomingQuantityByProduct(Location location) {
		return getQuantityByProduct(getIncomingOrders(location))
	}

	/**
	 * Returns a list of outgoing quantity per product given location.
	 * @param location
	 * @return
	 */
	Map getOutgoingQuantityByProduct(Location location) {
		return getQuantityByProduct(getOutgoingOrders(location))
	}
  
	
	/**
	 * Returns a map of order quantities per product given a list of orders.
	 * 
	 * @param orders
	 * @return
	 */
	Map getQuantityByProduct(def orders) {
		def quantityMap = [:]
		orders.each { order ->
			order.orderItems.each { orderItem ->
				def product = orderItem.product
				if (product) {
					def quantity = quantityMap[product];
					if (!quantity) quantity = 0;
					quantity += orderItem.quantity;
					quantityMap[product] = quantity
				}
			}
		}
		return quantityMap;
	}
	
	/**
	*
	* @param shipments
	* @return
	*/
   Map<EventType, ListCommand> getOrdersByStatus(List orders) {
	   def orderMap = new TreeMap<OrderStatus, ListCommand>();
	   
	   OrderStatus.list().each {
		   orderMap[it] = [];
	   }
	   	   
	   orders.each {
		   def key = it.status;
		   def orderList = orderMap[key];
		   if (!orderList) {
			   orderList = new ListCommand(category: key, objectList: new ArrayList());
		   }
		   orderList.objectList.add(it);
		   orderMap.put(key, orderList)
	   }
	   return orderMap;
   }

	
	
}
