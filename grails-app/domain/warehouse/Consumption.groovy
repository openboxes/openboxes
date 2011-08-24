package warehouse

import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;

class Consumption {

	Product product
	InventoryItem inventoryItem
	Location location
	int day
	int month
	int year
	Date transactionDate 
	Integer quantity
	
	Date lastUpdated
	Date dateCreated
			
    static constraints = {
    }
}
