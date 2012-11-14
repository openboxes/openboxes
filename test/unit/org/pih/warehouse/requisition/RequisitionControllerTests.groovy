

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
        def location1 = new Location(id:"1234", name: "zoom", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location3 = new Location(id:"1236", name: "hoom", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location2 = new Location(id:"1235", supportedActivities: ["supplier"])
        def myLocation = new Location(id:"001", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        mockDomain(Location, [location1, location2, myLocation, location3])
        mockDomain(Requisition, [])
        controller.params.name = "peter"
        controller.session.warehouse = myLocation

        def model = controller.edit()

        assert model.requisition.name == "peter"
        assert model.depots[0] == location3
        assert model.depots[1] == location1
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
        def requisition = new Requisition(id: "2345", lastUpdated: new Date(), status: RequisitionStatus.CREATED, version: 3)
        def requisitionItem = new RequisitionItem(id:"3322", orderIndex: 1)
        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem, [requisitionItem])
        requisition.addToRequisitionItems(requisitionItem)

        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.saveRequisition { data, location ->
            requisition
        }
        controller.requisitionService = requisitionServiceMock.createMock()

        Location userLocation = new Location(id:"boston")
        mockDomain(Location, [userLocation])
        controller.session.warehouse = userLocation
        controller.request.contentType = 'text/json' 
        controller.request.content ='{"id":"2345"}'

        controller.save()
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)

        assert jsonResponse.success
        assert jsonResponse.id == requisition.id
        assert jsonResponse.lastUpdated
        assert jsonResponse.status == requisition.status.toString()
        assert jsonResponse.version == requisition.version
        assert jsonResponse.requisitionItems.size() == 1
        assert jsonResponse.requisitionItems[0].id == requisitionItem.id
        assert jsonResponse.requisitionItems[0].orderIndex == requisitionItem.orderIndex

        requisitionServiceMock.verify()
    }

     void testSaveWithErrors() {
        def requisition = new Requisition(id: "2345")
        def requisitionItem = new RequisitionItem(id:"3322", orderIndex: 1)
        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem, [requisitionItem])
        mockForConstraintsTests(Requisition)
        requisition.addToRequisitionItems(requisitionItem)

        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.saveRequisition { data, location ->
            requisition.validate()
            requisition
        }
        controller.requisitionService = requisitionServiceMock.createMock()

        Location userLocation = new Location(id:"boston")
        mockDomain(Location, [userLocation])
        controller.session.warehouse = userLocation
        controller.request.contentType = 'text/json' 
        controller.request.content ='{"id":"2345"}'

        controller.save()
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)

        assert !jsonResponse.success
        assert jsonResponse.errors
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
