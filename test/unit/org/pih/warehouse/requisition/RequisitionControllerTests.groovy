

package org.pih.warehouse.requisition

import grails.test.ControllerUnitTestCase
import org.springframework.mock.web.MockHttpServletResponse
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import grails.converters.JSON
import org.pih.warehouse.core.ActivityCode
import testutils.MockBindDataMixin

@Mixin(MockBindDataMixin)
class RequisitionControllerTests extends ControllerUnitTestCase{

    protected void setUp(){
        super.setUp()
        mockBindData()
    }

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
        stubMessager.message = { args -> return "saved" }
        controller.metaClass.warehouse = stubMessager;
        def requisitionItemsToSave
        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.saveRequisition { requisition ->
            requisitionItemsToSave = requisition.requisitionItems?.collect{it}
        }
        controller.requisitionService = requisitionServiceMock.createMock()

        def location1 = new Location(id:"1234", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location2 = new Location(id:"1235", supportedActivities: ["supplier"])
        def myLocation = new Location(id:"001", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        mockDomain(Location, [location1, location2, myLocation])
        mockDomain(Requisition, [])

        def person = new Person(id:"1234adb")
        mockDomain(Person, [person])

        def requisition = new Requisition(id: "abcd")
        mockDomain(Requisition, [requisition])

        def requisitionItem1 = new RequisitionItem(orderIndex: 0)
        def requisitionItem2 = new RequisitionItem(orderIndex: 1, quantity: 500, product: new Product())
        mockDomain(RequisitionItem, [])

        controller.params.name = "testRequisition"
        controller.params.id = requisition.id
        controller.params.origin = location1
        controller.params.destination = myLocation
        controller.params.requestedBy = person
        controller.session.warehouse = myLocation
        controller.params["requisitionItems[0]"] = requisitionItem1
        controller.params["requisitionItems[1]"] = requisitionItem2

        controller.save()
        def model = renderArgs.model

        assert model.requisition.name == "testRequisition"
        assert model.requisition.origin.id == location1.id
        assert model.requisition.destination.id == myLocation.id
        assert model.requisition.requestedBy.id == person.id
        assert requisitionItem1.orderIndex != null
        assert model.requisition.requisitionItems.size() == 1
        assert model.requisitionItems.size() == 2
        assert model.requisitionItems.any{ it.orderIndex == requisitionItem1.orderIndex}
        assert model.requisitionItems.any{it.orderIndex == requisitionItem2.orderIndex}

        assert requisitionItemsToSave.size() == 1
        assert !requisitionItemsToSave.any{ it.orderIndex == requisitionItem1.orderIndex}
        assert requisitionItemsToSave.any{it.orderIndex == requisitionItem2.orderIndex}
        assert model.depots.contains(location1)
        assert !model.depots.contains(location2)
        assert !model.depots.contains(myLocation)

        assert renderArgs.view == "edit"
        assert controller.flash.message == "saved"


        requisitionServiceMock.verify()
    }

    void testDelete() {

        def location1 = new Location(id:"1234", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location2 = new Location(id:"1235", supportedActivities: ["supplier"])
        mockDomain(Location, [location1, location2])

        def person = new Person(id:"1234adb")
        mockDomain(Person, [person])

        def stubMessager = new Expando()
        stubMessager.message = { args -> return "deleted" }
        controller.metaClass.warehouse = stubMessager;

        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.deleteRequisition { true }
        controller.requisitionService = requisitionServiceMock.createMock()

        def requisition = new Requisition(id: "1234", name: "jim", origin: location1, destination: location2, requestedBy:person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1) )
        mockDomain(Requisition, [requisition])
        int oldSize = Requisition.count()

        controller.params.id = "1234"
        controller.delete()

        requisitionServiceMock.verify()
        assert redirectArgs.action == "list"
        assert controller.flash.message == "deleted"

        //This is a bad test because hibernate remembers the object even though it has been deleted. The domain does not refresh.
        //assertEquals oldSize - 1, Requisition.count()

    }



}
