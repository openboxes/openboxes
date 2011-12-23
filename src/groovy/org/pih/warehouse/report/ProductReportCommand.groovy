package org.pih.warehouse.report

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;

class ProductReportCommand {

	Integer quantityInitial
	Integer quantityFinal
	Product product
	Location location;
	Date startDate;
	Date endDate;	
	
	List<InventoryItem> inventoryItems = []
	List<ProductReportEntryCommand> productReportEntryList = []
			
	static constraints = {
		quantityInitial(nullable:false)
		quantityFinal(nullable:false)
		product(nullable:false)
		location(nullable:false)
		startDate(nullable:true)
		endDate(nullable:true)
	}
}