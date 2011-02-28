package org.pih.warehouse.order.cart

import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;

class CartController {

	def index = { 
		redirect(action: 'list')
	}
	
	def list = { 	
		def map = [:]	
		if (!session.cart) { 
			session.cart = new Cart();		
		}
		session.cart.items.each {			
			def product = Product.get(it?.product?.id) 
			map.put(product, it.getQuantity());
		}
		
		[ productInstanceMap : map ]
	}
	
	def addToCart = { 
		log.info params
		def inventoryInstance = Inventory.get(params?.inventory?.id);
		def productInstance = Product.get(params?.product?.id);		;
		def inventoryItemInstance = InventoryItem.get(params?.inventoryItem?.id);
		if (productInstance && inventoryInstance && inventoryItemInstance) {			
			if (!session?.cart) { 
				session.cart = new Cart();
			} 
			session.cart.addItem(new CartItem(product: productInstance, 
				inventory: inventoryInstance, 
				inventoryItem: inventoryItemInstance, quantity: (params.quantity?:1)));
			
			flash.message = "Product added to cart"	
			
			
		}
		else { 
			flash.message = "Product or inventory not found"			
		}
		
		if (params.redirectUrl) {
			//redirect(controller: "inventoryItem", action: "showStockCard", id: productInstance.id)
			redirect(url: params.redirectUrl);
			return;
		}
		
		redirect(controller: "catalog", action: "list", params: params)
		
	}

	def removeFromCart = {
		def productInstance = Product.get(params?.product?.id);
		if (productInstance) {
			if (!session?.cart) {
				session.cart = new Cart();
			}
			session.cart.clearItem(new CartItem(product: productInstance))
			flash.message = "Product removed from cart"
		}
		else {
			flash.message = "Product not found"
			
		}
		redirect(controller: "catalog", action: "list", params: params)
		
	}

	
		
}
