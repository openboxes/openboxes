package org.pih.warehouse.product

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryLevel;

class BatchEditCommand {
	
	//Location location
	Category rootCategory;
	List productInstanceList =
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Product.class));
	List categoryInstanceList =
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Category.class));
	
	//Map<Product, InventoryLevel> inventoryLevelMap = [:]
		
	static constraints = {
		//location(nullable:false)
		rootCategory(nullable:true)
	}
	
	//InventoryLevel getInventoryLevel(Product product) { 
	//	return inventoryLevelMap[product]
	//}
	
}

