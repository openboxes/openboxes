import java.util.ArrayList;
import java.util.Date;
import org.pih.warehouse.Country;
import org.pih.warehouse.Inventory;
import org.pih.warehouse.InventoryLineItem;
import org.pih.warehouse.Product;
import org.pih.warehouse.StockCard;
import org.pih.warehouse.Transaction;
import org.pih.warehouse.TransactionEntry;
import org.pih.warehouse.TransactionType;
import org.pih.warehouse.User;
import org.pih.warehouse.Warehouse;

class BootStrap {

    def init = { servletContext ->

	/**
	 * Countries
	 */
	new Country(country: "Canada", population: 24251210, gdp: 24251210, date: new Date()).save();
	new Country(country: "Haiti", population: 29824821, gdp: 24251210, date: new Date()).save();
	new Country(country: "Mexico", population: 103593973, gdp: 24251210, date: new Date()).save();
	new Country(country: "United States", population: 300000000, gdp: 24251210, date: new Date()).save();

	/**
	 * Users
	 */
	User user1 = new User(
	    email:"jmiranda@pih.org", 
	    firstName:"Justin", 
	    lastName:"Miranda",
	    role:"Stock Manager", 
	    username:"jmiranda", 
	    password: "password"
	).save();

	/**
	 * Products
	 */
	Product product1 = new Product(
	    ean:"03600029145X",
	    productCode:"1",
	    name: "Advil 200mg",
	    description: "Ibuprofen 200 mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	).save();

 	Product product2 = new Product(
	    ean:"03600022425X",
	    productCode:"2",
	    name: "Tylenol 325mg",
	    description: "Acetominophen 325 mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	).save();

	Product product3 = new Product(
	    ean:"02600058245X",
	    productCode:"3",
	    name: "Asprin",
	    description: "Asprin 20mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	).save(flush:true);

	/**
	 * Warehouses
	 */
	Warehouse warehouse1 = new Warehouse(
	    name: "Boston Headquarters",
	    city: "Boston, MA",
	    country: "United States",
	    manager: user1
	).save(flush:true);

	Warehouse warehouse2 = new Warehouse(
	    name: "Miami Warehouse",
	    city: "Miami, FL",
	    country: "United States",
	    manager: user1
	).save(flush:true);

	Warehouse warehouse3 = new Warehouse(
	    name: "Tabarre Depot",
	    city: "Tabarre",
	    country: "Haiti",
	    manager: user1
	).save(flush:true);

	/**
	 * warehouse > inventory > inventory items
	 */
	// Create new inventory
	Inventory inventory1 = new Inventory(
	    warehouse:warehouse3,
	    lastInventoryDate: new Date()
	).save(flush:true);
	
	// Create new inventory item
	InventoryLineItem inventoryItem1 = new InventoryLineItem(
	    product: product1,	    
	    quantity: 100,
	    reorderQuantity: 50,
	    idealQuantity: 100,
	    binLocation: "Warehouse Bin A1"
	).save(flush:true);
	inventory1.addToInventoryLineItems(inventoryItem1).save(flush:true, validate:false);

	/**
	 * warehouse > transactions > transaction entries
	 */
	TransactionType transactionType1 = new TransactionType(
	    name:"Incoming Shipment"
	).save(flush:true, validate:true);

	Transaction transaction1 = new Transaction(
	    transactionDate:new Date(),
	    //localWarehouse:warehouse2,
	    targetWarehouse:warehouse1,
	    transactionType:transactionType1
	); // removed .save(flush:true);
	warehouse3.addToTransactions(transaction1).save();

	TransactionEntry transactionEntry1 = new TransactionEntry(
	    product:product1,
	    quantityChange:50,
	    confirmDate:new Date()
	);
	transaction1.addToTransactionEntries(transactionEntry1).save(flush:true, validate:false);
	

     def destroy = {
     }
     
    }
    
}
