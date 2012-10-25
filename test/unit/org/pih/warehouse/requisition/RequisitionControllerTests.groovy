package org.pih.warehouse.requisition

import grails.test.ControllerUnitTestCase
import org.springframework.mock.web.MockHttpServletResponse
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

/**
 * Created by IntelliJ IDEA.
 * User: adminuser
 * Date: 10/25/12
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */
class RequisitionControllerTests extends ControllerUnitTestCase{

    void testEdit(){
        controller.params.name = "peter"
        def model =  controller.edit()

        assert model.requisition.name == "peter"
    }

    void testSave() {

        def stubMessager = new Expando()
        stubMessager.message = { args -> return "created" }
        controller.metaClass.warehouse = stubMessager;

        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.save { requisition -> requisition.id = "6677" }
        controller.requisitionService = requisitionServiceMock.createMock()

        def location = new Location(id:"1234")
        mockDomain(Location, [location])

        def person = new Person(id:"1234adb")
        mockDomain(Person, [person])

        controller.params.name = "testRequisition"
        controller.params.origin = [id: location.id]
        controller.params.destination = [id:location.id]
        controller.params.requestedBy = [id:person.id]

        controller.save()
        def model = renderArgs.model

        assert model.requisition.name == "testRequisition"
        assert model.requisition.origin.id == location.id
        assert model.requisition.destination.id == location.id
        assert model.requisition.requestedBy.id == person.id

        assert renderArgs.view == "edit"
        assert controller.flash.message == "created"


        requisitionServiceMock.verify()
    }


}
