package org.pih.warehouse



class InventoryService {

    boolean transactional = true

    List<Warehouse> getAllWarehouses() {
	return Warehouse.list()
    }

    List<Transaction> getAllTransactions(Warehouse warehouse) {
	return Transaction.withCriteria {
	    eq("localWarehouse", warehouse)
	}
    }

    Inventory getInventory(Warehouse warehouse) {
	return Inventory.withCriteria {
	    eq("warehouse", warehouse)
	}
    }

}
