package org.pih.warehouse.report

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;

class InventoryReportCommand {

	Location location;
	Date startDate;
	Date endDate;	
	Category category;
	
	Map<Product, InventoryReportEntryCommand> inventoryReportEntryMap = [:]
	
	static constraints = {
		location(nullable:false)
		startDate(nullable:true)
		endDate(nullable:true)
		category(nullable:true)
	}
}