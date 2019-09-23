/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core

import grails.test.*
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.TransactionCode;
import org.pih.warehouse.inventory.TransactionType
// import Location
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product


class BaseUnitTest extends GrailsUnitTestCase {
	protected void setUp() {
        super.setUp()
        
		

          // create some default location types
        def warehouseLocationType = new LocationType(name: "Location", description: "Location")
        def supplierLocationType= new LocationType(name: "Supplier", description: "Supplier")
        mockDomain(LocationType, [ warehouseLocationType, supplierLocationType ])

        // create a default location
        def acmeSupplyCompany = new Location(name: "Acme Supply Company", locationType: supplierLocationType)
        
        // create some default warehouses and inventories
        def bostonLocation = new Location(name: "Boston Location", locationType: warehouseLocationType)
        def haitiLocation = new Location(name: "Haiti Location", locationType: warehouseLocationType)
    
        def bostonLocationInventory = new Inventory(warehouse: bostonLocation)
        def haitiLocationInventory = new Inventory(warehouse: haitiLocation)
        
        bostonLocation.inventory = bostonLocationInventory
        haitiLocation.inventory = haitiLocationInventory
        
        mockDomain(Location, [ bostonLocation, haitiLocation, acmeSupplyCompany ] )
        //mockDomain(Inventory, [ bostonLocationInventory, haitiLocationInventory ])
       
        // create some default transaction types
        def consumptionTransactionType = new TransactionType(id: 2, name: "Consumption", transactionCode: TransactionCode.DEBIT)
        def inventoryTransactionType = new TransactionType(id: 7, name: "Inventory", transactionCode: TransactionCode.INVENTORY)
        def productInventoryTransactionType = new TransactionType(id: 11, name: "Product Inventory", transactionCode: TransactionCode.PRODUCT_INVENTORY)
        def transferInTransactionType = new TransactionType(id: Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, name: "Transfer In", transactionCode: TransactionCode.CREDIT)
        def transferOutTransactionType = new TransactionType(id: Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, name: "Transfer Out", transactionCode: TransactionCode.DEBIT)
        mockDomain(TransactionType, [ consumptionTransactionType, productInventoryTransactionType, inventoryTransactionType, transferInTransactionType, transferOutTransactionType ])
        
		
		def category = new Category(id: "1", name: "Pain Medication")
		mockDomain(Category, [category])
		
        // create some products
        def aspirin = new Product(name: "Aspirin", category: category)
        def tylenol = new Product(name:"Tylenol", category: category)
        mockDomain(Product, [aspirin, tylenol])
        
        // create some inventory items
        def aspirinLot1 = new InventoryItem(product: aspirin, lotNumber: "1")
        def aspirinLot2 = new InventoryItem(product: aspirin, lotNumber: "2")
        def tylenolLot1 = new InventoryItem(product: tylenol, lotNumber: "1")
        mockDomain(InventoryItem, [aspirinLot1, aspirinLot2, tylenolLot1])
  
    }

    protected void tearDown() {
        super.tearDown()
    }
	
	void testDataHasBeenInitialized() {
		assertEquals 2, LocationType.list().size()
		assertEquals 3, Location.list().size()
		assertEquals 5, TransactionType.list().size()
		assertEquals 2, Product.list().size()
		assertEquals 3, InventoryItem.list().size()		
	}
	
}
