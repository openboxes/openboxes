package org.pih.warehouse.report

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;

class ProductReportCommand {

	Product product
	Location location;
	Date startDate;
	Date endDate;	
	
	Integer quantityInitial
	Integer quantityFinal

	List<InventoryItem> inventoryItems = []
	List<ProductReportEntryCommand> productReportEntryList = []
			
	static constraints = {
		product(nullable:false)
		location(nullable:false)
		startDate(nullable:true)
		endDate(nullable:true)
		quantityInitial(nullable:true)
		quantityFinal(nullable:true)
	}
}