package org.pih.warehouse.report

import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;

class InventoryReportCommand {

	Product product
	Boolean includeChildren
	Boolean showEntireHistory
	Location location;
	Date startDate;
	Date endDate;	
	Category category;
	Category rootCategory;
	
	Map<Product, InventoryReportEntryCommand> entries = [:]


	static constraints = {
		showEntireHistory(nullable:true)
		product(nullable:true)
		location(nullable:false)
		startDate(nullable:true)
		endDate(nullable:true)
		category(nullable:false)
		includeChildren(nullable:true)
	}
	
	
	InventoryReportEntryCommand getProductEntry(Product product) { 
		return entries[product]
	}
	
	Set<Product> getProducts() {		
		return entries.keySet()
	}
	
	Collection getProducts(Category category) {
		return getProductsByCategory()[category]
	}
	
	Map getProductsByCategory() {
		return getProducts()?.groupBy { it.category } 
	}
	
	
	String toString() { 
		return "Location: " + location	
	}
	
}