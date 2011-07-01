package org.pih.warehouse.inventory;

import grails.validation.ValidationException;

import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.grails.plugins.excelimport.ExcelImportUtils;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.ProductAttribute;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Constants 
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType;
import org.springframework.validation.Errors;

/**
 * Stateful session bean
 * 
 * @author jmiranda
 */
class QuantityService {

	// Indicates that the service is session-scoped (it stores state) and proxied (so it can be used injected/used by 
	// prototype-scoped objects like taglibs).
	static scope = "session"
	static proxy = true
	
	Warehouse warehouse
	InventoryService inventoryService
	
	Integer getQuantity(Product product, String lotNumber) { 
		if (!warehouse) { 
			throw new RuntimeException("Your warehouse has not been initialized");
		}
		else { 
			warehouse = Warehouse.get(warehouse?.id)
		}
		def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, lotNumber)
		if (!inventoryItem) { 
			throw new RuntimeException("There's no inventory item for product " + product?.name + " lot number " + lotNumber)
		}
		
		return inventoryService.getQuantityForInventoryItem(inventoryItem, warehouse.inventory)
	}

}
