package org.pih.warehouse.inventory

import grails.converters.JSON
import grails.test.ControllerUnitTestCase
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryItemControllerTests extends ControllerUnitTestCase {

    void test_showRecordInventory() {

        def inventory = new Inventory(id: "inventory1")
        def myLocation = new Location(id: "1234", inventory: inventory)
        Product p = new Product(id:"pro1", name:"product1");
        RecordInventoryCommand com = new RecordInventoryCommand(productInstance: p, inventoryInstance: inventory)
        InventoryItem i1 = new InventoryItem(id: "item1", product: p)
        InventoryItem i2 = new InventoryItem(id: "item2", product: p)
        InventoryItem i3 = new InventoryItem(id: "item3", product: p)
        InventoryItem i4 = new InventoryItem(id: "item4", product: p)
        mockDomain(Product, [p])
        mockDomain(InventoryItem, [i1, i2, i3, i4])
        mockDomain(InventoryLevel, [])
        mockCommandObject(RecordInventoryCommand)
        mockDomain(Location, [myLocation])
        mockDomain(Inventory, [inventory])

        def inventoryServiceMock = mockFor(InventoryService)
        inventoryServiceMock.demand.populateRecordInventoryCommand(1) { commandInstance, userInventory ->

        }
        inventoryServiceMock.demand.getTransactionEntriesByInventoryAndProduct(1) { inventoryInstance, productInstance ->

        }
        inventoryServiceMock.demand.getQuantityByProductMap(1) { transactionEntryList ->
            return [:]
        }
        inventoryServiceMock.demand.getInventoryItemsWithQuantity(1) { product, inv ->
            return [product: [id:"pro1", name:"product1"], inventoryItems: [[id:"item1"], [id:"item2"], [id:"item3"], [id:"item4"]]]
        }
        controller.inventoryService = inventoryServiceMock.createMock()

        controller.params.id = p.id
        controller.session.warehouse = myLocation
        def model = controller.showRecordInventory(com)

        assert model.inventoryItems
        println(model);
        println("data:" + model.inventoryItems)
        def json = JSON.parse(model.inventoryItems)

        assert json.product.id == "pro1"
        assert json.inventoryItems.size() == 4
        assert json.inventoryItems[2].id == "item3"

    }
}
