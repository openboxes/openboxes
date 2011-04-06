package org.pih.warehouse.core

import grails.test.*

import org.pih.warehouse.inventory.Inventory 
import org.pih.warehouse.inventory.InventoryItem 
import org.pih.warehouse.inventory.TransactionCode;
import org.pih.warehouse.inventory.TransactionType 
import org.pih.warehouse.inventory.Warehouse 
import org.pih.warehouse.product.Product 


class BaseUnitTest extends GrailsUnitTestCase {
	protected void setUp() {
        super.setUp()
        
          // create some default location types
        def warehouseLocationType = new LocationType(code: "locationType.warehouse", name: "Warehouse", description: "Warehouse")
        def supplierLocationType= new LocationType(code: "locationType.supplier", name: "Supplier", description: "Supplier")
        mockDomain(LocationType, [ warehouseLocationType, supplierLocationType ])

        // create a default location
        def acmeSupplyCompany = new Location(name: "Acme Supply Company", locationType: supplierLocationType) 
        mockDomain(Location, [ acmeSupplyCompany ])
        
        // create some default warehouses and inventories
        def bostonWarehouse = new Warehouse(name: "Boston Warehouse", locationType: warehouseLocationType)
        def haitiWarehouse = new Warehouse(name: "Haiti Warehouse", locationType: warehouseLocationType)
    
        def bostonWarehouseInventory = new Inventory(warehouse: bostonWarehouse)
        def haitiWarehouseInventory = new Inventory(warehouse: haitiWarehouse)
        
        bostonWarehouse.inventory = bostonWarehouseInventory
        haitiWarehouse.inventory = haitiWarehouseInventory
        
        mockDomain(Warehouse, [ bostonWarehouse, haitiWarehouse ] )
        mockDomain(Inventory, [ bostonWarehouseInventory, haitiWarehouseInventory ])
       
        // create some default transaction types
        def consumptionTransactionType = new TransactionType(id: 2, name: "Consumption")
        def inventoryTransactionType = new TransactionType(id: 7, name: "Inventory", transactionCode: TransactionCode.INVENTORY)
        def transferInTransactionType = new TransactionType(id: Constants.TRANSFER_IN_TRANSACTION_TYPE_ID, name: "Transfer In", transactionCode: TransactionCode.CREDIT)
        def transferOutTransactionType = new TransactionType(id: Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID, name: "Transfer Out", transactionCode: TransactionCode.DEBIT)
        mockDomain(TransactionType, [ inventoryTransactionType, transferInTransactionType, transferOutTransactionType ])
        
        // create some products
        def aspirin = new Product(name: "Aspirin")
        mockDomain(Product, [aspirin])
        
        // create some inventory items
        def aspirinLot1 = new InventoryItem(product: aspirin, lotNumber: "1")
        mockDomain(InventoryItem, [aspirinLot1])
  
    }

    protected void tearDown() {
        super.tearDown()
    }
}
