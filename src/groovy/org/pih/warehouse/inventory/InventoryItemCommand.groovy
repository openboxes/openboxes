package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.ProductGroup;


class InventoryItemCommand {
	
	String description
	Category category
	Product product 
	ProductGroup productGroup
	InventoryItem inventoryItem
	InventoryLevel inventoryLevel
	
	// For product groups, we need to keep track of all product-level inventory items
	List<InventoryItemCommand> inventoryItems
	
	Integer quantityOnHand = 0;
	Integer quantityToShip = 0;
	Integer quantityToReceive = 0;
	
	static constraints = {
		category(nullable: true) 
		product(nullable:true)
		productGroup(nullable:true)
		inventoryItem(nullable:true)
		inventoryLevel(nullable:true)
		inventoryItems(nullable:true)
		quantityOnHand(nullable:true)
		quantityToShip(nullable:true)
		quantityToReceive(nullable:true)
	}
	
	/**
	 * An item is supported if it 
	 * @return
	 */
	Boolean getSupported() { 
		return !inventoryLevel?.status || inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.SUPPORTED
	}
	
	Boolean getNotSupported() { 
		return inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.NOT_SUPPORTED ||
			inventoryLevel?.status == org.pih.warehouse.inventory.InventoryStatus.SUPPORTED_NON_INVENTORY
	}
	
	
	int hashCode() {
		if (product != null) {
			return product.id.hashCode();
		}
		return super.hashCode();
	}
	
	boolean equals(Object o) {
		if (o instanceof InventoryItemCommand) {
			InventoryItemCommand that = (InventoryItemCommand)o;
			return this.product.id == that.product.id;
		}
		return false;
	}
}



