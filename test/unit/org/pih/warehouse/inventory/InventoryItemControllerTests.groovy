package org.pih.warehouse.inventory

import grails.converters.JSON
import grails.test.ControllerUnitTestCase
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product


@TestFor(InventoryItemController)
@Mock([Product, Location, Inventory, InventoryService])
class InventoryItemControllerTests {

    @Test
    void test_showRecordInventory() {

        def inventory = new Inventory(id: "inventory1")
        def myLocation = new Location(id: "1234", inventory: inventory)
        Product p = new Product(id:"pro1", name:"product1");
        InventoryItem i1 = new InventoryItem(id: "item1", product: p)
        InventoryItem i2 = new InventoryItem(id: "item2", product: p)
        InventoryItem i3 = new InventoryItem(id: "item3", product: p)
        InventoryItem i4 = new InventoryItem(id: "item4", product: p)

        RecordInventoryCommand com = new RecordInventoryCommand(productInstance: p, inventoryInstance: inventory)

        def inventoryServiceMock = mockFor(InventoryService)
        inventoryServiceMock.demand.populateRecordInventoryCommand(1) { commandInstance, userInventory -> }
        inventoryServiceMock.demand.getTransactionEntriesByInventoryAndProduct(1) { inventoryInstance, productInstance -> }
        inventoryServiceMock.demand.getQuantityByProductMap(1) { transactionEntryList -> return [:] }
        inventoryServiceMock.demand.getInventoryItemsWithQuantity(1) { product, inv ->
            return ["pro1": [[id:"item1"], [id:"item2"], [id:"item3"], [id:"item4"]]]
        }
        controller.inventoryService = inventoryServiceMock.createMock()

        controller.params.id = p.id
        controller.session.warehouse = myLocation
        def model = controller.showRecordInventory(com)

        assert model["product"]
        def json = JSON.parse(model["product"])

        assert json.product
        assert json.inventoryItems
    }
}
