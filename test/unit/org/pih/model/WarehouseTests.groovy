package org.pih.model

import grails.test.*
import java.util.ArrayList;
import org.pih.warehouse.Product;
import org.pih.warehouse.User;
import org.pih.warehouse.Warehouse;
import org.pih.warehouse.StockCard;
import org.pih.warehouse.Inventory;
import org.pih.warehouse.InventoryLineItem;
import org.pih.warehouse.Transaction;
import org.pih.warehouse.TransactionEntry;
import org.pih.warehouse.TransactionType;


class WarehouseTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSomething() {


	/**
	 * Users
	 */
	User user1 = new User(
	    id: 1,
	    email:"jmiranda@pih.org",
	    firstName:"Justin",
	    lastName:"Miranda",
	    role:"Stock Manager",
	    username:"jmiranda",
	    password: "password"
	);
	user1.save();


	/**
	 * Products
	 */
	Product product1 = new Product(
	    id: 1,
	    ean:"03600029145X",
	    productCode:"1",
	    name: "Advil 200mg",
	    description: "Ibuprofen 200 mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	)
	product1.save(flush:true);

 	Product product2 = new Product(
	    id: 2,
	    ean:"03600022425X",
	    productCode:"2",
	    name: "Tylenol 325mg",
	    description: "Acetominophen 325 mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	)
	product2.save(flush:true);

	Product product3 = new Product(
	    id: 3,
	    ean:"02600058245X",
	    productCode:"3",
	    name: "Asprin",
	    description: "Asprin 20mg",
	    category: "Pain Reliever",
	    user: user1,
	    stockCard: new StockCard()
	)
	product3.save(flush:true);


	/**
	 * Warehouses
	 */
	Warehouse warehouse1 = new Warehouse(
	    id: 1,
	    name: "Miami",
	    city: "Miami",
	    country: "United States",
	    manager: user1
	)
	warehouse1.save(flush:true);

	Warehouse warehouse2 = new Warehouse(
	    id: 2,
	    name: "Tabarre",
	    city: "Tabarre",
	    country: "Haiti",
	    manager: user1
	)
	warehouse2.save(flush:true);


	/**
	 * warehouse > inventory > inventory items
	 */
	// Create new inventory
	Inventory inventory2 = new Inventory(
	    id: 2,
	    warehouse:warehouse2
	);
	inventory2.save(flush:true);
	warehouse2.setInventory(inventory2);
	warehouse2.save(flush:true);


	// Create new inventory item
	InventoryLineItem inventoryLineItem1 = new InventoryLineItem(
	    product: product1,
	    quantity: 100,
	    reorderQuantity: 50,
	    idealQuantity: 100,
	    binLocation: "Warehouse Bin A1"
	);

	// TRYING THIS APPROACH
	inventory2.addToInventoryLineItems(inventoryLineItem1).save(flush:true, validate:false);
	inventory2.save(flush:true);

	// Save warehouse
	warehouse2.setInventory(inventory2);
	warehouse2.save(flush:true);

	/**
	 * warehouse > transactions > transaction entries
	 */
	TransactionType transactionType1 = new TransactionType(name:"Shipment");
	transactionType1.save(flush:true);

	TransactionEntry transactionEntry1 = new TransactionEntry(
	    id:1,
	    product:product1,
	    //transaction:transaction1,
	    quanityChange:new Integer(50),
	    confirmDate:new Date()
	);

	// New transaction
	Transaction transaction1 = new Transaction(
	    id:1,
	    transactionDate:new Date(),
	    //localWarehouse:warehouse2,
	    targetWarehouse:warehouse1,
	    transactionType:transactionType1
	);
	transaction1.addToTransactionEntries(transactionEntry1).save();
	warehouse2.addToTransactions(transaction1).save();


    }
}
