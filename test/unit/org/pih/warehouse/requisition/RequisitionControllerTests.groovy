package org.pih.warehouse.requisition

import grails.test.ControllerUnitTestCase
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.springframework.mock.web.MockHttpServletResponse
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.picklist.*
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

    void testEditShouldRenderDepots(){
        def location1 = new Location(id:"1234", name: "zoom", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location3 = new Location(id:"1236", name: "hoom", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        def location2 = new Location(id:"1235", supportedActivities: ["supplier"])
        def myLocation = new Location(id:"001", supportedActivities: [ActivityCode.MANAGE_INVENTORY])
        mockDomain(Location, [location1, location2, myLocation, location3])
         def requisition = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition])
        controller.params.id = requisition.id
        controller.session.warehouse = myLocation

        def model = controller.edit()

        assert model.depots[0] == location3
        assert model.depots[1] == location1
        assert !model.depots.contains(location2)
        assert !model.depots.contains(myLocation)
    }

    void testEditExistingRequisition(){
        def requisition = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition])
        mockDomain(Location, [])
        controller.params.id = "1234"
        def model =  controller.edit()
        def json = JSON.parse(model.requisition)
        assert model.requisitionId == requisition.id
        assert json.id == requisition.toJson().id
        assert json.name == requisition.toJson().name
    }

     void testEditRequisitionWhichCannotBeFound(){
        def requisition = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition])
        mockDomain(Location, [])
        controller.params.id = "something cannot be found"
        controller.edit()
        assert controller.response.status == 404
    }


    void testSave() {
        def requisition = new Requisition(id: "2345", lastUpdated: new Date(), status: RequisitionStatus.CREATED, version: 3)
        def requisitionItem = new RequisitionItem(id:"3322", orderIndex: 1, version: 3)
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
        assert jsonResponse.data.id == requisition.id
        assert jsonResponse.data.lastUpdated
        assert jsonResponse.data.status == requisition.status.toString()
        assert jsonResponse.data.version == requisition.version
        assert jsonResponse.data.requisitionItems.size() == 1
        assert jsonResponse.data.requisitionItems[0].id == requisitionItem.id
        assert jsonResponse.data.requisitionItems[0].orderIndex == requisitionItem.orderIndex
        assert jsonResponse.data.requisitionItems[0].version == requisitionItem.version

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

    def testCreate(){
      mockDomain(Location, [])
      def today = new Date().format("MM/dd/yyyy")
      def tomorrow = new Date().plus(1).format("MM/dd/yyyy")
      controller.create()
      assert renderArgs.view == "edit"
      assert renderArgs.model
      assert renderArgs.model.depots == []
      assert renderArgs.model.requisition
      String requisitionString = renderArgs.model.requisition
      def requisitionJson = JSON.parse(requisitionString)
      assert requisitionJson.dateRequested == today
      assert requisitionJson.requestedDeliveryDate == tomorrow 

    }

    def testProcess() {
        def requisition = new Requisition(id: "req1")
        mockDomain(Requisition, [requisition])
        mockDomain(RequisitionItem, [])
        def inventory = new Inventory(id: "inventory1")
        def myLocation = new Location(id: "1234", inventory: inventory)
        def product = new Product(id: "prod1", name:"peter's product")
        def inventoryItem = new InventoryItem(id: "inventoryItem1", lotNumber: "inventLot1")
        def picklist = new Picklist(id:"picklist1", requisition: requisition)
        mockDomain(Location, [myLocation])
        mockDomain(Inventory, [inventory])
        mockDomain(Product, [product])
        mockDomain(InventoryItem, [inventoryItem])
        mockDomain(Picklist, [picklist])

        def inventoryServiceMock = mockFor(InventoryService)
        inventoryServiceMock.demand.getInventoryItemsWithQuantity(2..2) { products, userInventory ->
            def map = [:]
            map[product] = [inventoryItem]
            map
        }
        controller.inventoryService = inventoryServiceMock.createMock()
        controller.params.id = requisition.id
        controller.session.warehouse = myLocation
        def model = controller.process()
        def json = JSON.parse(model.data)

        assert json.requisition.id == requisition.id
        assert json.productInventoryItemsMap
        assert json.productInventoryItemsMap[product.id]
        assert json.productInventoryItemsMap[product.id].size() == 1
        assert json.productInventoryItemsMap[product.id].first().inventoryItemId == inventoryItem.id
        assert json.picklist
        assert json.picklist.id == picklist.id


    }

    void testProcessRequisitionWhichCannotBeFound(){
        def requisition = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition])
        mockDomain(Location, [])
        controller.params.id = "something cannot be found"
        controller.process()
        assert controller.response.status == 404
    }

    void testCancelRequisitionWhichCannotBeFound() {
        def requisition = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition])
        mockDomain(Location, [])
        controller.params.id = "something cannot be found"
        controller.cancel()
        assert controller.redirectArgs.action == "list"
    }

    void testCancelExistingRequisitionDuringEdit() {
        def requisition = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition])
        mockDomain(Location, [])

        def stubMessager = new Expando()
        stubMessager.message = { args -> return "cancelled" }
        controller.metaClass.warehouse = stubMessager;
        
        def requisitionServiceMock = mockFor(RequisitionService)
        requisitionServiceMock.demand.deleteRequisition { true }
        controller.requisitionService = requisitionServiceMock.createMock()
        controller.params.id = "1234"
        controller.delete()

        requisitionServiceMock.verify()
        assert redirectArgs.action == "list"
        assert controller.flash.message == "cancelled"
    }

    void testListRequisitions() {

        def requisition = new Requisition(id: "req1", name: "req1", recipientProgram:"abc")
        def requisition2 = new Requisition(id: "req2", name: "req2", recipientProgram:"abcde")
        def requisition3 = new Requisition(id: "1234", name: "jim", recipientProgram:"abc")
        mockDomain(Requisition, [requisition, requisition2, requisition3])

        controller.list()

        assert renderArgs.view == "list"
        assert renderArgs.model
        assert renderArgs.model.requisitions
        assert renderArgs.model.requisitions.size() == 3
        assert renderArgs.model.requisitions.any { it.id = requisition.id}
        assert renderArgs.model.requisitions.any { it.id = requisition2.id}
        assert renderArgs.model.requisitions.any { it.id = requisition3.id}

    }
}
