package org.pih.warehouse

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Role;
import org.pih.warehouse.core.User;

class PicklistTagLib {
   	
	def inventoryService
	
	def picklistItem = { attrs, body ->
		
		def location = Location.get(session.warehouse.id)
		attrs.products = []
		if (attrs.requestItem.product) { 
			attrs.product = attrs.requestItem.product
			attrs.inventoryItems = inventoryService.findInventoryItemsByProduct(attrs.product)
			attrs.inventoryItems.each { 
				it.quantityOnHand = inventoryService.getQuantity(location.inventory, it)
				it.quantityAvailableToPromise = inventoryService.getQuantityAvailableToPromise(location.inventory, it)
			}
			attrs.inventoryItems = attrs.inventoryItems.findAll { it.quantityOnHand > 0 } 
			attrs.inventoryItem = attrs.inventoryItems.find { it?.expirationDate?.after(new Date()) }
			
		}
		else if (attrs.requestItem.category) { 
			attrs.products = attrs.requestItem.category.products
			attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
		}
		else if (attrs.requestItem.productGroup) {
			attrs.products = attrs.requestItem.productGroup.products			
			attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
		}
		

		out << g.render(template: '/taglib/pickRequestItem', model: [attrs:attrs])
	}
	
	
	def mapRequestItem = { attrs, body ->
		attrs.products = []
		if (attrs.requestItem.product) {
			attrs.product = attrs.requestItem.product
			println "product " + attrs.product
			attrs.inventoryItems = inventoryService.findInventoryItemsByProduct(attrs.product)
			attrs.inventoryItem = attrs.inventoryItems.find { it.expirationDate != null }
		}
		else if (attrs.requestItem.category) {
			attrs.products = attrs.requestItem.category.products
			println "products " + attrs.products
			attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
		}
		else if (attrs.requestItem.productGroup) {
			attrs.products = attrs.requestItem.productGroup.products
			println "products " + attrs.products
			attrs.inventoryItems = inventoryService.findInventoryItemsByProducts(attrs.products)
		}
		

		out << g.render(template: '/taglib/mapRequestItem', model: [attrs:attrs])
	}
}
