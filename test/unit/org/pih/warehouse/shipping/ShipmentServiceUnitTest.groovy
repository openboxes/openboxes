package org.pih.warehouse.shipping

import grails.test.GrailsUnitTestCase
import org.joda.time.DateTime
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

class ShipmentServiceUnitTest extends GrailsUnitTestCase {

    private oneYearFromNow = new DateTime().plusYears(1).toDate();
    private twoYearsFromNow = new DateTime().plusYears(2).toDate();

    private Shipment shipment
    private Container containerOne
    private Container containerTwo
    private Container containerThree
    private Product productA

    private InventoryItem inventoryItemProductALotABC
    private InventoryItem inventoryItemProductALotDEF
    private ShipmentItem shipmentItemContainerOneProductALotABC
    private ShipmentItem shipmentItemContainerOneProductALotDEF
    private ShipmentItem shipmentItemContainerThreeProductALotABC
    private ShipmentItem shipmentItemUnpackedItemProductALotABC

    private ArrayList<ShipmentItem> allShipmentItems

    @Override
    protected void setUp() {
        super.setUp()

        productA = new Product(id: "productIdA")

        shipment = new Shipment(id: "shipmentId")
        mockDomain(Shipment, [shipment]);

        containerOne = new Container(id : 'containerOneId')
        shipment.addToContainers(containerOne)

        containerTwo = new Container(id : 'containerTwoId')
        shipment.addToContainers(containerTwo)

        containerThree = new Container(id : 'containerThreeId')
        shipment.addToContainers(containerThree)

        mockDomain(Container, [containerOne, containerTwo, containerThree]);

        inventoryItemProductALotABC = new InventoryItem(product: productA, lotNumber: 'ABC',  expirationDate: oneYearFromNow);
        inventoryItemProductALotDEF = new InventoryItem(product: productA, lotNumber: 'DEF',  expirationDate: twoYearsFromNow);


        shipmentItemContainerOneProductALotABC = new ShipmentItem(id: 'containerOneProductALotABC',
                                            shipment : shipment,
                                            container: containerOne,
                                            product : productA,
                                            lotNumber: 'ABC',
                                            inventoryItem: inventoryItemProductALotABC,
                                            quantity:  50);
        shipment.addToShipmentItems(shipmentItemContainerOneProductALotABC)

        shipmentItemContainerOneProductALotDEF = new ShipmentItem(id: 'containerOneProductALotDEF',
                shipment : shipment,
                container: containerOne,
                product : productA,
                lotNumber: 'DEF',
                inventoryItem: inventoryItemProductALotDEF,
                quantity:  50);
        shipment.addToShipmentItems(shipmentItemContainerOneProductALotDEF)


        shipmentItemContainerThreeProductALotABC = new ShipmentItem(id: 'containerThreeProductALotABC',
                shipment : shipment,
                container: containerThree,
                product : productA,
                lotNumber: 'ABC',
                inventoryItem: inventoryItemProductALotABC,
                quantity:  30);
        shipment.addToShipmentItems(shipmentItemContainerThreeProductALotABC)

        shipmentItemUnpackedItemProductALotABC = new ShipmentItem(id: 'unpackedItemProductALotABC',
                shipment : shipment,
                product : productA,
                lotNumber: 'ABC',
                inventoryItem: inventoryItemProductALotABC,
                quantity:  100);
        shipment.addToShipmentItems(shipmentItemUnpackedItemProductALotABC)

        allShipmentItems = [shipmentItemContainerOneProductALotABC,
                shipmentItemContainerOneProductALotDEF,
                shipmentItemContainerThreeProductALotABC,
                shipmentItemUnpackedItemProductALotABC]
        mockDomain(ShipmentItem, allShipmentItems)
    }

    public void test_moveItem_shouldMoveItemFromContainerToContainer() {

        // Move all of containerOne's productA/lot ABC to containerTwo which contains none
        def quantityInContainerOne = shipmentItemContainerOneProductALotABC.quantity

        def service = new ShipmentService()
        def returnValue = service.moveItem(shipmentItemContainerOneProductALotABC, ["containerTwoId": quantityInContainerOne]);

        assertTrue("moveItem should return true", returnValue)

        Set<ShipmentItem> matchingItems = shipment.shipmentItems.findAll {
            it.product == productA &&
            it.inventoryItem == inventoryItemProductALotABC
        }

        assertFalse("Item should not longer exist in container one", matchingItems.any { it.container == containerOne })
        assertEquals(1, matchingItems.findAll { it.container == containerTwo }.size())

        def containerTwoItem = matchingItems.find { it.container == containerTwo }

        assertEquals(quantityInContainerOne, containerTwoItem.quantity);
        assertEquals(containerTwo, containerTwoItem.container);
    }

    public void test_moveItem_shouldIncrementQuantityWhenSameInventoryItemIsThere() {

        def quantityInContainerOne = shipmentItemContainerOneProductALotABC.quantity
        def quantityInContainerThree = shipmentItemContainerThreeProductALotABC.quantity

        def service = new ShipmentService()
        def returnValue = service.moveItem(shipmentItemContainerOneProductALotABC, ["containerThreeId": quantityInContainerOne]);

        assertTrue("moveItem should return true", returnValue)

        Set<ShipmentItem> matchingItems = shipment.shipmentItems.findAll {
            it.product == productA &&
            it.inventoryItem == inventoryItemProductALotABC
        }

        assertFalse("Item should not longer exist in container one", matchingItems.any { it.container == containerOne })

        assertEquals(1, matchingItems.findAll { it.container == containerThree }.size())
        def containerThreeItem = matchingItems.find { it.container == containerThree }

        assertEquals(quantityInContainerOne + quantityInContainerThree, containerThreeItem.quantity);
    }

    public void test_moveItem_shouldIncrementQuantityWhenSameProductDifferentInventoryItemIsThere() {

        def quantityInContainerOne = shipmentItemContainerOneProductALotDEF.quantity

        def service = new ShipmentService()
        def returnValue = service.moveItem(shipmentItemContainerOneProductALotDEF, ["containerThreeId": quantityInContainerOne]);

        assertTrue("moveItem should return true", returnValue)

        Set<ShipmentItem> matchingItemsContainerThree = shipment.shipmentItems.findAll {
            it.container == containerThree &&
            it.product == productA &&
            it.inventoryItem == inventoryItemProductALotDEF
        }

        def containerThreeItem = matchingItemsContainerThree.iterator().next()

        assertEquals(1, matchingItemsContainerThree.size())
        assertEquals(quantityInContainerOne, containerThreeItem.quantity);
    }

    public void test_moveItem_shouldMoveItemFromUnpackedItems() {

        def quantityInUnpackedItem = shipmentItemUnpackedItemProductALotABC.quantity

        def service = new ShipmentService()
        def returnValue = service.moveItem(shipmentItemUnpackedItemProductALotABC, ["containerTwoId": quantityInUnpackedItem]);

        assertTrue("moveItem should return true", returnValue)

        Set<ShipmentItem> matchingItems = shipment.shipmentItems.findAll {
            it.container == containerTwo &&
            it.product == productA &&
            it.inventoryItem == inventoryItemProductALotABC
        }
        def containerTwoItem = matchingItems.iterator().next()

        assertEquals(1, matchingItems.size())
        assertEquals(quantityInUnpackedItem, containerTwoItem.quantity);

    }

    public void test_moveItem_shouldMoveItemToUnpackedItems() {
        def quantityInContainerThree = shipmentItemContainerThreeProductALotABC.quantity
        def quantityInUnpackedItem = shipmentItemUnpackedItemProductALotABC.quantity


        def service = new ShipmentService()
        def returnValue = service.moveItem(shipmentItemContainerThreeProductALotABC, ["0": quantityInContainerThree]);

        assertTrue("moveItem should return true", returnValue)

        Set<ShipmentItem> matchingItems = shipment.shipmentItems.findAll {
            it.container == null &&
            it.product == productA &&
            it.inventoryItem == inventoryItemProductALotABC
        }
        assertEquals(1, matchingItems.size())

        def unpackedItem = matchingItems.iterator().next()
        assertEquals(quantityInUnpackedItem + quantityInContainerThree, unpackedItem.quantity);
    }

}
