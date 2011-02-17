package org.pih.warehouse.inventory

class BrowseInventoryCommand {
	
	def warehouseInstance;	//warehouseInstance,
	def inventoryInstance; 	//warehouseInstance.inventory,
	def categoryInstance; 	//categoryInstance,
	def categoryFilters; 	//categoryFilters,
	def productMap; 		//inventoryService.getProductMap(warehouseInstance?.id),
	def inventoryItemMap;	 //inventoryService.getInventoryItemMap(warehouseInstance?.id),
	def productList;		//productList?.sort() { it.name },
	def rootCategory;		//rootCategory
	def quantityMap;
	def shipmentList;		// pending shipments
	
	
	static constraints = { 
		warehouseInstance(nullable:true)
		inventoryInstance(nullable:true)
		categoryInstance(nullable:true)
		categoryFilters(nullable:true)
		productMap(nullable:true)
		inventoryItemMap(nullable:true)
		productList(nullable:true)
		shipmentList(nullable:true)
		rootCategory(nullable:true)		
		quantityMap(nullable:true)
	}
	
}

