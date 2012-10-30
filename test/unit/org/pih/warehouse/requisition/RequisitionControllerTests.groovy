

package org.pih.warehouse.requisition

import grails.test.ControllerUnitTestCase
import org.springframework.mock.web.MockHttpServletResponse
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import grails.converters.JSON

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
        requisitionServiceMock.demand.saveRequisition { requisition -> requisition.id = "6677" }
        controller.requisitionService = requisitionServiceMock.createMock()

        def location = new Location(id:"1234")
        def myLocation = new Location(id:"001")
        mockDomain(Location, [location, myLocation])

        def person = new Person(id:"1234adb")
        mockDomain(Person, [person])

        controller.params.name = "testRequisition"
        controller.params.origin = [id: location.id]
        controller.params.destination = [id:location.id]
        controller.params.requestedBy = [id:person.id]
        controller.session.warehouse = myLocation

        controller.save()
        def model = renderArgs.model

        assert model.requisition.name == "testRequisition"
        assert model.requisition.origin.id == location.id
        assert model.requisition.destination.id == myLocation.id
        assert model.requisition.requestedBy.id == person.id

        assert renderArgs.view == "edit"
        assert controller.flash.message == "created"


        requisitionServiceMock.verify()
    }

    def testSaveItemSucceeded() {

        def product = new Product(id:"product1")
        mockDomain(Product, [product])

        def requisition = new Requisition(id:"requisition1")
        mockDomain(Requisition, [requisition])

        def person = new Person(id:"person1")
        mockDomain(Person, [person])

        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.saveRequisitionItem { requisitionItem -> requisitionItem.id = "abc3344" }
        controller.requisitionService = requisitionServiceMock.createMock()

        controller.params.requisition = [id:requisition.id]
        controller.params.product = [id:product.id]
        controller.params.substitutable = true
        controller.params.recipient = [id:person.id]
        controller.params.comment = "testRequisitionItem"
        controller.params.quantity = 50

        controller.saveRequisitionItem()

        def jsonResponse = JSON.parse(controller.response.contentAsString)

        assert jsonResponse.requisitionItem.id == "abc3344"
        assert jsonResponse.success == true

    }

    def testSaveItemFailed() {
        mockForConstraintsTests(RequisitionItem)
        def product = new Product(id:"product1")
        mockDomain(Product, [product])

        def requisition = new Requisition(id:"requisition1")
        mockDomain(Requisition, [requisition])

        def person = new Person(id:"person1")
        mockDomain(Person, [person])

        def requisitionServiceMock = mockFor(RequisitionService)
        def bindedItem;
        requisitionServiceMock.demand.saveRequisitionItem { requisitionItem ->
            bindedItem = requisitionItem
            requisitionItem.validate() }
        controller.requisitionService = requisitionServiceMock.createMock()


        controller.saveRequisitionItem()

        def jsonResponse = JSON.parse(controller.response.contentAsString)


        assert jsonResponse.success == false
        def firstError = jsonResponse.errors.first()
        assert firstError.field
        assert firstError.defaultMessage
        assert firstError.arguments




    }


}
