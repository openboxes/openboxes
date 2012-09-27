package org.pih.warehouse.shipping

import grails.test.*

class CreateShipmentWorkflowControllerTests extends ControllerUnitTestCase {


    void testMakeDestinationMap_shouldAllowMovingToUnpackedItems() {
        def containerOne = new Container(id: 'containerOneId')
        mockDomain(Container, [containerOne])

        mockDomain(Shipment)
        def shipment = new Shipment(id: "shipmentId")
        shipment.addToContainers(containerOne)

        def itemBeingMoved = new ShipmentItem(shipment: shipment, container: containerOne);
        mockDomain(ShipmentItem, [itemBeingMoved])

        def params = ['quantity-0': '50', 'quantity-containerOneId': '50']

        def map = CreateShipmentWorkflowController.makeDestinationMap(itemBeingMoved, params)

        assertEquals(["0": 50], map);
    }

    void testMakeDestinationMap_shouldAllowMovingFromUnpackedItems() {
        def containerOneGuid = 'containerOneId'

        def containerOne = new Container(id: containerOneGuid)
        mockDomain(Container, [containerOne])

        mockDomain(Shipment)
        def shipment = new Shipment(id: "shipmentId")
        shipment.addToContainers(containerOne)

        def unpackedItem = new ShipmentItem(shipment: shipment);
        mockDomain(ShipmentItem, [unpackedItem])

        def params = ['quantity-0': "50", 'quantity-containerOneId': "50"]

        def map = CreateShipmentWorkflowController.makeDestinationMap(unpackedItem, params)

        assertEquals(['containerOneId': 50], map);
    }

    void testMakeDestinationMap_shouldAllowMovingToAnotherContainer() {
        mockDomain(Shipment);
        mockDomain(Container);
        mockDomain(ShipmentItem);

        def containerOne = new Container(id: 'containerOneId')
        def containerTwo = new Container(id: 'containerTwoId')

        def shipment = new Shipment()
        shipment.addToContainers(containerOne)
        shipment.addToContainers(containerTwo)

        def itemBeingMoved = new ShipmentItem(shipment: shipment, container: containerOne);

        def params = ["quantity-0": "0", "quantity-containerOneId": "0", "quantity-containerTwoId": "50"]

        def map = CreateShipmentWorkflowController.makeDestinationMap(itemBeingMoved, params)

        assertEquals(["containerTwoId": 50], map);
    }

}
