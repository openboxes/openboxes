package org.pih.warehouse.shipping

import grails.test.ControllerUnitTestCase
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.pih.warehouse.MessageTagLib
import grails.buildtestdata.mixin.Build
import org.pih.warehouse.core.Location
import spock.lang.Specification

@Build([Shipment, Container, Location])
@TestFor(ShipmentController)
class ShipmentControllerTests extends Specification {

    static shipmentServiceMock

    void setup() {
        shipmentServiceMock = mockFor(ShipmentService)
        def stubMessageTagLib = new Expando()
        stubMessageTagLib.message = { message -> return message }
        controller.metaClass.warehouse = stubMessageTagLib;

        shipmentServiceMock.demand.addToShipment { true }
        controller.shipmentService = shipmentServiceMock.createMock()
    }

    void tearDown() { }


    void 'show action should redirect to show details'() {
        when:
        params.id = 1
        controller.show()

        then:
        assertTrue response.redirectedUrl == '/shipment/showDetails/1'
    }

    void testAddToShipmentUnpackedItemContainerShouldRedirectToCreateShipmentFlow() {
        when:
        def shipmentId = 'SHIP001'
        def origin = Location.build(name: "Origin 1")
        def destination = Location.build(name: "Destination1 1")
        Shipment.build(id: 'SHIP001', name: "Shipment 1", origin: origin, destination: destination)
        //Container.build(id: "CONT001", name: "Container 1")

        mockCommandObject(ItemListCommand)

        ItemListCommand commands = new ItemListCommand()
        commands.items = [new ItemCommand()]

        controller.params.shipmentContainerKey = "$shipmentId:0"
        controller.addToShipmentPost(commands)

        then:
        response.redirectedUrl == "/createShipmentWorkflow/createShipment/SHIP001?skipTo=Packing&containerId="
    }

    void testAddToShipmentPackingListContainerShouldRedirectToCreateShipmentFlow() {

        when:
        def shipmentId = 'SHIP001'
        def containerId = 'CONT001'
        def origin = Location.build(name: "Origin 2")
        def destination = Location.build(name: "Destination 2")
        Shipment.build(id: 'SHIP001', name: "Shipment 1", origin: origin, destination: destination)
        //Container.build(id: "CONT001", name: "Container 1")
        mockCommandObject(ItemListCommand)

        ItemListCommand commands = new ItemListCommand()
        commands.items = [new ItemCommand()]

        controller.params.shipmentContainerKey = "$shipmentId:$containerId"
        controller.addToShipmentPost(commands)


        then:
        response.redirectedUrl == "/createShipmentWorkflow/createShipment/SHIP001?skipTo=Packing&containerId="

    }
}
