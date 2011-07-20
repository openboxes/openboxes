package org.pih.warehouse.inventory

class InventoryCommand {
	
	def inventoryItems;
	def searchTerms;		// request-level search terms  
	def warehouseInstance;	// warehouseInstance,
	def inventoryInstance; 	// warehouseInstance.inventory,
	def categoryInstance; 	// categoryInstance,

	def showHiddenProducts; 
	def searchTermFilters 	// search filters loaded from the session
	def categoryFilters; 	// category filters loaded from the session
	//def productMap; 		// inventoryService.getProductMap(productList),
	//def inventoryItemMap;	// inventoryService.getInventoryItemMap(warehouseInstance?.id),
	def products;		// productList?.sort() { it.name },
	def rootCategory;		// rootCategory
	//def quantityMap;
	//def shipmentList;		// pending shipments
	
	
	static constraints = { 
		products(nullable:true)
		inventoryItems(nullable:true)
		showHiddenProducts(nullable:true)
		warehouseInstance(nullable:true)
		inventoryInstance(nullable:true)
		categoryInstance(nullable:true)
		searchTermFilters(nullable:true)
		categoryFilters(nullable:true)
		//productMap(nullable:true)
		//inventoryItemMap(nullable:true)
		//productList(nullable:true)
		//shipmentList(nullable:true)
		rootCategory(nullable:true)		
		//quantityMap(nullable:true)
	}
	
}



