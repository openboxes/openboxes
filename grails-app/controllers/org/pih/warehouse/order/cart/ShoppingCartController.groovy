package org.pih.warehouse.order.cart

import org.pih.warehouse.product.Product;

class ShoppingCartController {

	def index = { 
		redirect(action: 'list')
	}
	
	def list = { 	
		def map = [:]	
		if (!session.cart) { 
			session.cart = new ShoppingCart();		
		}
		session.cart.itemIds.each {			
			def product = Product.get(it) 
			map.put(product, session.cart.getQuantity(it));
		}
		
		[ productInstanceMap : map ]
	}
	
	def addToCart = { 
		def productInstance = Product.get(params?.product?.id);		
		if (productInstance) {			
			if (!session?.cart) { 
				session.cart = new ShoppingCart();
			} 
			session.cart.addItem(productInstance?.id)
			flash.message = "Product added to cart"	
		}
		else { 
			flash.message = "Product not found"
			
		}
		redirect(controller: "catalog", action: "list", params: params)
		
	}

	def removeFromCart = {
		def productInstance = Product.get(params?.product?.id);
		if (productInstance) {
			if (!session?.cart) {
				session.cart = new ShoppingCart();
			}
			session.cart.clearItem(productInstance?.id)
			flash.message = "Product removed from cart"
		}
		else {
			flash.message = "Product not found"
			
		}
		redirect(controller: "catalog", action: "list", params: params)
		
	}

	
		
}
