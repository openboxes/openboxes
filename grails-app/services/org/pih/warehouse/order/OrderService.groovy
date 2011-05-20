package org.pih.warehouse.order

import org.pih.warehouse.order.cart.Cart;

class OrderService {

	boolean transactional = true
	

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
