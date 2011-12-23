package org.pih.warehouse.report

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;

class InventoryReportEntryCommand {

	Product product;
	Integer quantityInitial = 0;
	Integer quantityRunning = 0;
	Integer quantityFinal = 0;
	
	Integer quantityFound = 0;
	Integer quantityTransferredIn = 0;
	Integer quantityTotalIn = 0;
	
	Integer quantityTransferredOut = 0;
	Integer quantityConsumed = 0;
	Integer quantityDamaged = 0;
	Integer quantityExpired = 0;
	Integer quantityLost = 0;
	Integer quantityTotalOut = 0;
		
	Map<Location, Integer> quantityTransferredOutByLocation = [:]
	
	
	
	static constraints = {
	}
}