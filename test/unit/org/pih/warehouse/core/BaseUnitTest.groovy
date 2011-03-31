package org.pih.warehouse.core

import grails.test.*

import org.pih.warehouse.inventory.Inventory 
import org.pih.warehouse.inventory.LocalTransfer 
import org.pih.warehouse.inventory.Transaction 
import org.pih.warehouse.inventory.TransactionType 
import org.pih.warehouse.inventory.Warehouse 


class BaseUnitTest extends GrailsUnitTestCase {
	protected void setUp() {
        super.setUp()
        
          // create some default location types
        def warehouseLocationType = new LocationType(code: "locationType.warehouse", name: "Warehouse", description: "Warehouse")
        def supplierLocationType= new LocationType(code: "locationType.supplier", name: "Supplier", description: "Supplier")
        mockDomain(LocationType, [ warehouseLocationType, supplierLocationType ])

        // create a default location
        def acmeSupplyCompany = new Location(name: "ACME Supply Company", locationType: supplierLocationType) 
        mockDomain(Location, [ acmeSupplyCompany ])
        
        // create some default warehouses
        def bostonWarehouse = new Warehouse(name: "Boston Warehouse", locationType: warehouseLocationType)
        def haitiWarehouse = new Warehouse(name: "Haiti Warehouse", locationType: warehouseLocationType)
        mockDomain(Warehouse, [ bostonWarehouse, haitiWarehouse ] )
        
        // create some default inventories
        def bostonWarehouseInventory = new Inventory(warehouse: bostonWarehouse)
        def haitiWarehouseInventory = new Inventory(warehouse: haitiWarehouse)
        mockDomain(Inventory, [ bostonWarehouseInventory, haitiWarehouseInventory ])
        
        // create some default transaction types
        def inventoryTransactionType = new TransactionType(id: 7, name: "Inventory")
        def transferInTransactionType = new TransactionType(id: 8, name: "Transfer In")
        def transferOutTransactionType = new TransactionType(id: 9, name: "Transfer Out")
        mockDomain(TransactionType, [ inventoryTransactionType, transferInTransactionType, transferOutTransactionType ])
        
        // create some test transactions
        mockDomain(Transaction, [ new Transaction(id: 10, transactionType: inventoryTransactionType, transactionDate: new Date(), inventory: bostonWarehouseInventory),
                                 new Transaction(id: 20, transactionType: transferInTransactionType, transactionDate: new Date(), inventory: bostonWarehouseInventory, source: acmeSupplyCompany),
                                 new Transaction(id: 30, transactionType: transferOutTransactionType, transactionDate: new Date(), inventory: bostonWarehouseInventory, destination: acmeSupplyCompany),
                                 new Transaction(id: 40, transactionType: transferInTransactionType, transactionDate: new Date(), inventory: bostonWarehouseInventory, source: haitiWarehouse),
                                 new Transaction(id: 50, transactionType: transferOutTransactionType, transactionDate: new Date(), inventory: bostonWarehouseInventory, destination: haitiWarehouse)])
        
        // create the (empty) LocalTransfer domain
        mockDomain(LocalTransfer)
        
    }

    protected void tearDown() {
        super.tearDown()
    }
}
