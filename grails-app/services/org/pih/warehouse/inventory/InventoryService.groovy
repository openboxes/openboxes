package org.pih.warehouse.inventory;

import org.pih.warehouse.Transaction;
import org.pih.warehouse.Warehouse;

class InventoryService {
	
	boolean transactional = true
	
	List<Warehouse> getAllWarehouses() {
		return Warehouse.list()
	}
	
	List<Transaction> getAllTransactions(Warehouse warehouse) {
		return Transaction.withCriteria { eq("localWarehouse", warehouse) }
	}
	
	Inventory getInventory(Warehouse warehouse) {
		return Inventory.withCriteria { eq("warehouse", warehouse) }
	}
	
	Warehouse getWarehouse(Long id) { 
		return Warehouse.get(id);
	}
}
