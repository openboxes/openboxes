package org.pih.warehouse.core

import grails.test.*

import org.pih.warehouse.inventory.Inventory 
import org.pih.warehouse.inventory.InventoryItem 
import org.pih.warehouse.inventory.TransactionCode;
import org.pih.warehouse.inventory.TransactionType 
import org.pih.warehouse.core.Location 
import org.pih.warehouse.product.Product 


class BaseUnitTest extends GrailsUnitTestCase {
	protected void setUp() {
        super.setUp()
        
          // create some default location types
        def warehouseLocationType = new LocationType(code: "locationType.warehouse", name: "Location", description: "Location")
        def supplierLocationType= new LocationType(code: "locationType.supplier", name: "Supplier", description: "Supplier")
        mockDomain(LocationType, [ warehouseLocationType, supplierLocationType ])

        // create a default location
        def acmeSupplyCompany = new Location(name: "Acme Supply Company", locationType: supplierLocationType) 
        mockDomain(Location, [ acmeSupplyCompany ])
        
        // create some default warehouses and inventories
        def bostonLocation = new Location(name: "Boston Location", locationType: warehouseLocationType)
        def haitiLocation = new Location(name: "Haiti Location", locationType: warehouseLocationType)
    
        def bostonLocationInventory = new Inventory(warehouse: bostonLocation)
        def haitiLocationInventory = new Inventory(warehouse: haitiLocation)
        
        bostonLocation.inventory = bostonLocationInventory
        haitiLocation.inventory = haitiLocationInventory
        
        mockDomain(Location, [ bostonLocation, haitiLocation ] )
        mockDomain(Inventory, [ bostonLocationInventory, haitiLocationInventory ])
       
        // create some default transaction types
        def consumptionTransactionType = new TransactionType(id: 2, name: "Consumption", transactionCode: TransactionCode.DEBIT)
        def inventoryTransactionType = new TransactionType(id: 7, name: "Inventory", transactionCode: TransactionCode.INVENTORY)
        def productInventoryTransactionType = new TransactionType(id: 11, name: "Product Inventory", transactionCode: TransactionCode.PRODUCT_INVENTORY)
        def transferInTransactionType = new TransactionType(id: Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, name: "Transfer In", transactionCode: TransactionCode.CREDIT)
        def transferOutTransactionType = new TransactionType(id: Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, name: "Transfer Out", transactionCode: TransactionCode.DEBIT)
        mockDomain(TransactionType, [ consumptionTransactionType, productInventoryTransactionType, inventoryTransactionType, transferInTransactionType, transferOutTransactionType ])
        
        // create some products
        def aspirin = new Product(name: "Aspirin")
        def tylenol = new Product(name:"Tylenol")
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
}
