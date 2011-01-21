package org.pih.warehouse.catalog

import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Warehouse;

class CatalogController {

	//def scaffold = Inventory
	def inventoryService;
		
	def index = {
		redirect(action: "browse");
	}
	
	
	def list = { 	
		// Get the warehouse from the request parameter
		def warehouseInstance = Warehouse.get(params?.warehouse?.id)
		
		// If it doesn't exist or if the parameter is null, get
		// warehouse from the session
		if (!warehouseInstance) {
			warehouseInstance = Warehouse.get(session?.warehouse?.id);
		}
		
		[
			warehouseInstance: warehouseInstance,
			inventoryInstance: warehouseInstance.inventory,
			productMap : inventoryService.getProductMap(warehouseInstance?.id),
			inventoryMap : inventoryService.getInventoryItemMap(warehouseInstance?.id)
		]

	}
	
	
	
}
