package org.pih.warehouse.inventory;

import java.util.Map;

import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;

class InventoryService {
	
	boolean transactional = true
	
	List<Warehouse> getAllWarehouses() {
		return Warehouse.list()
	}
	
	List<Transaction> getAllTransactions(Warehouse warehouse) {
		return Transaction.withCriteria { eq("thisWarehouse", warehouse) }
	}
	
	Inventory getInventory(Warehouse warehouse) {
		return Inventory.withCriteria { eq("warehouse", warehouse) }
	}
	
	Map getProductMap(Long id) { 		
		// Get a warehouse specific product map
		//def warehouse = Warehouse.get(id);		
		/*		
		def products = Product.getAll();
		def productMap = products.inject([:]) { map, element ->
			def productType = element.productType
			if (productType) { 
				def productList = map[productType?.name]
				if (!productList) productList = new ArrayList<Product>();
				productList.add(element);
				map[productType?.name] = productList;
			}
			map
		}
		return productMap;
		*/
		
		return Product.getAll().groupBy { it.productType } 
	}
	
	
	/**
	 * @param productId
	 * @return a list of inventory items 
	 */
	List getInventoryItemsByProduct(Product productInstance) { 
		if (!productInstance) 
			throw new Exception("errors.product.ProductNotFoundException")
		def results = InventoryItem.findAllByProduct(productInstance)					
		return results;
	}
	
	List getTransactionEntriesByProduct(Product productInstance) { 		
		def results = TransactionEntry.findAllByProduct(productInstance)		
		return results;
	}
	
	
	Map getInventoryMap(Long id) { 
		
		/*
		def inventoryMap;
		def warehouse = Warehouse.get(id);
		if (warehouse && warehouse?.inventory) { 
			inventoryMap = warehouse?.inventory?.inventoryItems.inject([:]) { map, element ->				
				map[element.product] = element; 
			}
		}
		return inventoryMap;
		*/
		return Warehouse.get(id)?.inventory?.inventoryItems.groupBy { it.product } 
		
	}
	
	
	Warehouse getWarehouse(Long id) { 
		return Warehouse.get(id);
	}
}
