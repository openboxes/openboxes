package unit.org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.User
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentApiController
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType

@Unroll
class ShipmentApiControllerTests extends Specification implements ControllerUnitTest<ShipmentApiController>, DataTest {

    @Shared
    private ShipmentService shipmentServiceStub

    void setupSpec() {
        mockDomains(Shipment, Location, ShipmentType, User, LocationType, LocationGroup)
    }

    void setup() {
        shipmentServiceStub = Stub(ShipmentService)
        controller.shipmentService = shipmentServiceStub
    }

    void "list action should return shipments sorted by expectedShippingDate"() {
        given: "Mocked locations, shipment type, and shipments"
        def depotType = new LocationType(name: "Depot").save(validate: false)
        def bostonGroup = new LocationGroup(name: "Boston Group").save(validate: false)

        def originLocation = new Location(name: "Origin Warehouse", locationType: depotType, locationGroup: bostonGroup).save(validate: false)
        def destinationLocation = new Location(name: "Destination Warehouse", locationType: depotType, locationGroup: bostonGroup).save(validate: false)

        def shipmentType = new ShipmentType(name: "Air").save(validate: false)
        def user = new User(username: "JohnDoe").save(validate: false)

        def shipment1 = new Shipment(name: "Shipment A", expectedShippingDate: new Date() + 2, origin: originLocation, destination: destinationLocation, shipmentType: shipmentType, createdBy: user).save(validate: false)
        def shipment2 = new Shipment(name: "Shipment B", expectedShippingDate: new Date() + 1, origin: originLocation, destination: destinationLocation, shipmentType: shipmentType, createdBy: user).save(validate: false)
        def shipment3 = new Shipment(name: "Shipment C", expectedShippingDate: new Date() + 3, origin: originLocation, destination: destinationLocation, shipmentType: shipmentType, createdBy: user).save(validate: false)

        and: "Shipment service returns sorted shipments"
        shipmentServiceStub.getShipmentsByLocationAndRequisitionStatuses(_, _, _, _) >> [shipment2, shipment1, shipment3]

        and: "Session and params"
        session.warehouse = originLocation
        params.origin = originLocation.id
        params.destination = destinationLocation.id

        when: "The list action is invoked"
        controller.list()

        then: "The response contains the shipments sorted by expectedShippingDate"
        JSONObject jsonResponse = getJsonObjectResponse(controller.response)
        jsonResponse.data.size() == 3

        def shipmentDates = jsonResponse.data*.expectedShippingDate
        assert shipmentDates == shipmentDates.sort()

        and: "Verify order of shipments"
        jsonResponse.data[0].name == "Shipment B"
        jsonResponse.data[1].name == "Shipment A"
        jsonResponse.data[2].name == "Shipment C"
    }

    private static JSONObject getJsonObjectResponse(response) {
        response.status == 200
        return response.json as JSONObject
    }
}
