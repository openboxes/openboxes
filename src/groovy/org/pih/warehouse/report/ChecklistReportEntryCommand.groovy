package org.pih.warehouse.report

import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ShipmentItem;

class ChecklistReportEntryCommand {

	Product product;
	ShipmentItem shipmentItem
	InventoryItem inventoryItem
	String lotNumber
	Date expirationDate
	Integer quantityIn
	Integer quantityOut
	Integer balance
	
	
	static constraints = {
	}
	

	
}