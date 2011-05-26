package org.pih.warehouse.order

import org.pih.warehouse.core.Location;
import org.pih.warehouse.order.cart.Cart;

class OrderService {

	boolean transactional = true
	

	List<Order> getIncomingOrders(Location location) { 
		return Order.findAllByDestination(location)
	}

	List<Order> getOutgoingOrders(Location location) { 
		return Order.findAllByOrigin(location)
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
