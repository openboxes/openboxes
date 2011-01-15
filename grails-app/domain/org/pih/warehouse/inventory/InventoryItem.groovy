package org.pih.warehouse.inventory

import java.util.Date;

import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Transaction;

/**
 * Represents products that are usually stocked by this location.
 */
class InventoryItem {
	
	String description;						// Description of the specific instance of a product that we're tracking
	Product product;		    			// Product that we're tracking
	String lotNumber;						// Lot information for a product  
	String serialNumber						// Only used if this is a "serialized" item (e.g. equipment)
	InventoryItemType inventoryItemType		// Serialized or non-serialized
	Boolean active = Boolean.TRUE			// Actively managed
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
    // TODO Cannot have a reference to product for some reason
    static belongsTo = [ inventory : Inventory ];
	static transients = ['warnings', 'quantity', 'inventoryLot']
	
	// Notice the unique constraint on lotNumber/product
    static constraints = {
		description(nullable:false)
		product(nullable:false)
		lotNumber(nullable:false, unique:'product')
		serialNumber(nullable:true)
		inventoryItemType(nullable:false);
		active(nullable:false)
		
		//onHandQuantity(min:0, nullable:false)

    }
	
	
	List getWarnings() { 
		def warnings = new ArrayList<String>()
		/*
		def quantityOnHand = this.quantity;
		if (minQuantity && onHandQuantity <= quantityLow) { 
			warnings << "inventoryItem.lowStock.alert";
		}
		else { 
			if (quantityIdeal && quantityOnHand <= quantityIdeal) {
				warnings << "inventoryItem.idealQuantity.info";
				
			} 
			else { 
				if (quantityReorder && quantityOnHand <= quantityReorder) {
					warnings << "inventoryItem.reorder.alert";
				}
			}
		} 
		if (!expirationDate) { 
			warnings << "inventoryItem.noExpirationDate.warning"	
		} 
		else { 
			if (expirationDate <= new Date()) { 
				warnings << "inventoryItem.expiredStock.error";			
			} 
			else if (expirationDate <= (new Date()+90) && expirationDate >= (new Date()+61) ) { 
				warnings << "inventoryItem.expiringStock.info"				
			}
			else if (expirationDate <= (new Date()+60) && expirationDate >= (new Date()+31) ) { 
				warnings << "inventoryItem.expiringStock.warning"				
			}
			else if (expirationDate <= (new Date()+30) ) { 
				warnings << "inventoryItem.expiringStock.error"
			}
		}*/
		
		return warnings;
		
	}
	
	
	Integer getQuantity() { 
		//return TransactionEntry.findAllByInventoryItem(this).inject(0) { count, item -> count + (item?.quantity ?: 0) }
		return TransactionEntry.findAllByProductAndLotNumber(product, lotNumber).inject(0) { count, item -> count + (item?.quantity ?: 0) }
	}
	
	InventoryLot getInventoryLot() {
		InventoryLot.findByProductAndLotNumber(product, lotNumber);
	}
	

	
	
}
