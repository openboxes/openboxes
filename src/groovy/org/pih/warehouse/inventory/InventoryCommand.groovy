package org.pih.warehouse.inventory

class InventoryCommand {
	
	def warehouseInstance;								// warehouseInstance	
	def searchTerms;									// request-level search terms  
	def categoryInstance; 								// categoryInstance
	def subcategoryInstance;							// child category to show within the categoryInstance
	def showUnsupportedProducts = Boolean.FALSE;		// indicates whether unsupported products for the warehouse should be included
	def showNonInventoryProducts = Boolean.FALSE;		// indicates whether non-inventory products for the warehouse should be included
	def showOutOfStockProducts = Boolean.TRUE;			// indicates whether out of stock products for the warehouse should be included

	def categoryToProductMap = {};						// all of the resulting ProductCommands above, organized by Category
	
	static constraints = {
		warehouseInstance(nullable:true)
		searchTerms(nullable:true)
		categoryInstance(nullable:true)
		subcategoryInstance(nullable:true)
		showUnsupportedProducts(nullable:true)
		showNonInventoryProducts(nullable:true)
		showOutOfStockProducts(nullable:true)
		categoryToProductMap(nullable:true)
	}
}
