package org.pih.warehouse.inventory

class BrowseInventoryCommand {
	
	def searchTerms;		// request-level search terms  
	def warehouseInstance;	// warehouseInstance,
	def inventoryInstance; 	// warehouseInstance.inventory,
	def categoryInstance; 	// categoryInstance,

	def searchTermFilters 	// search filters loaded from the session
	def categoryFilters; 	// category filters loaded from the session
	def productMap; 		// inventoryService.getProductMap(warehouseInstance?.id),
	def inventoryItemMap;	// inventoryService.getInventoryItemMap(warehouseInstance?.id),
	def productList;		// productList?.sort() { it.name },
	def rootCategory;		// rootCategory
	def attributeMap;
	def quantityMap;
	def shipmentList;		// pending shipments
	
	
	static constraints = { 
		warehouseInstance(nullable:true)
		inventoryInstance(nullable:true)
		categoryInstance(nullable:true)
		searchTermFilters(nullable:true)
		categoryFilters(nullable:true)
		productMap(nullable:true)
		inventoryItemMap(nullable:true)
		productList(nullable:true)
		shipmentList(nullable:true)
		rootCategory(nullable:true)		
		quantityMap(nullable:true)
	}
	
}

