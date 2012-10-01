/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
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
