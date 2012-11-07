

package org.pih.warehouse.requisition

import grails.test.ControllerUnitTestCase
import org.springframework.mock.web.MockHttpServletResponse
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import grails.converters.JSON
import org.pih.warehouse.core.ActivityCode

/**
 * Created by IntelliJ IDEA.
 * User: adminuser
 * Date: 10/25/12
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */
class RequisitionControllerTests extends ControllerUnitTestCase{

    void testEdit(){
        def location1 = new Location(id:"1234", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location2 = new Location(id:"1235", supportedActivities: ["supplier"])
        def myLocation = new Location(id:"001", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        mockDomain(Location, [location1, location2, myLocation])
        mockDomain(Requisition, [])
        controller.params.name = "peter"
        controller.session.warehouse = myLocation

        def model = controller.edit()

        assert model.requisition.name == "peter"
        assert model.depots.contains(location1)
        assert !model.depots.contains(location2)
        assert !model.depots.contains(myLocation)
    }
    void testEditExistingRequisition(){
        def requisition = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition])
        mockDomain(Location, [])
        controller.params.name = "peter"
        controller.params.id = "1234"
        def model =  controller.edit()
        assert model.requisition.id == "1234"
        assert model.requisition.name == "peter"
        assert model.requisition.recipientProgram == "abc"
    }

    void testSave() {

        def stubMessager = new Expando()
        stubMessager.message = { args -> return "created" }
        controller.metaClass.warehouse = stubMessager;

        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.saveRequisition { requisition -> requisition.id = "6677" }
        controller.requisitionService = requisitionServiceMock.createMock()

        def location1 = new Location(id:"1234", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location2 = new Location(id:"1235", supportedActivities: ["supplier"])
        def myLocation = new Location(id:"001", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        mockDomain(Location, [location1, location2, myLocation])
        mockDomain(Requisition, [])

        def person = new Person(id:"1234adb")
        mockDomain(Person, [person])

        controller.params.name = "testRequisition"
        controller.params.origin = location1
        controller.params.destination = myLocation
        controller.params.requestedBy = person
        controller.session.warehouse = myLocation

        controller.save()
        def model = renderArgs.model

        assert model.requisition.name == "testRequisition"
        assert model.requisition.origin.id == location1.id
        assert model.requisition.destination.id == myLocation.id
        assert model.requisition.requestedBy.id == person.id

        assert model.depots.contains(location1)
        assert !model.depots.contains(location2)
        assert !model.depots.contains(myLocation)

        assert renderArgs.view == "edit"
        assert controller.flash.message == "created"


        requisitionServiceMock.verify()
    }



}
