package org.pih.warehouse.picklist

import grails.test.ControllerUnitTestCase
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import grails.converters.JSON
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

class PicklistControllerTests extends ControllerUnitTestCase{

  void testSave() {
        def picklist = new Picklist(id: "2345", version: 3)
        def requisitionItem = new RequisitionItem(id:"ri1")
        def inventoryItem = new InventoryItem(id: "ii1")
        def picklistItem = new PicklistItem(id:"3322", version: 3, requisitionItem: requisitionItem, inventoryItem: inventoryItem)
        mockDomain(Picklist, [picklist])
        mockDomain(PicklistItem, [picklistItem])
        picklist.addToPicklistItems(picklistItem)
        mockDomain(InventoryItem, [inventoryItem])
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistServiceMock = mockFor(PicklistService)
        picklistServiceMock.demand.save{ data ->
            picklist
        }
        controller.picklistService = picklistServiceMock.createMock()

        controller.request.contentType = 'text/json' 
        controller.request.content ='{"id":"2345"}'

        controller.save()
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)

        assert jsonResponse.success
        assert jsonResponse.data.id == picklist.id
        assert jsonResponse.data.version == picklist.version
        assert jsonResponse.data.picklistItems.size() == 1
        assert jsonResponse.data.picklistItems[0].id == picklistItem.id
        assert jsonResponse.data.picklistItems[0].requisitionItemId == requisitionItem.id
        assert jsonResponse.data.picklistItems[0].inventoryItemId == inventoryItem.id
        assert jsonResponse.data.picklistItems[0].version == picklistItem.version

        picklistServiceMock.verify()
    }

     void testSaveWithErrors() {
        def picklist = new Picklist(id: "2345")
        def picklistItem = new PicklistItem(id:"3322")
        mockDomain(Picklist, [picklist])
        mockDomain(PicklistItem, [picklistItem])
        mockForConstraintsTests(Picklist)
        picklist.addToPicklistItems(picklistItem)

        def picklistServiceMock = mockFor(PicklistService)
        picklistServiceMock.demand.save { data ->
            picklist.validate()
            picklist
        }
        controller.picklistService = picklistServiceMock.createMock()

        controller.request.contentType = 'text/json'
        controller.request.content ='{"id":"2345"}'

        controller.save()
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)

        assert !jsonResponse.success
        assert jsonResponse.errors
        picklistServiceMock.verify()
    }

    void testSaveWithRequisition() {
        def picklist = new Picklist(id: "2345")
        def picklistItem = new PicklistItem(id:"3322")
        def requisition = new Requisition(id: "r1")
        mockDomain(Picklist, [picklist])
        mockDomain(PicklistItem, [picklistItem])
        mockForConstraintsTests(Picklist)
        picklist.addToPicklistItems(picklistItem)
        mockDomain(Requisition, [requisition])
        picklist.requisition = requisition

        def picklistServiceMock = mockFor(PicklistService)
        picklistServiceMock.demand.save { data ->
            picklist.validate()
            picklist
        }
        controller.picklistService = picklistServiceMock.createMock()

        controller.request.contentType = 'text/json'
        controller.request.content ='{"id":"2345"}'

        controller.save()
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)

        assert jsonResponse.success
        picklistServiceMock.verify()
    }

    void testSaveWithOrder() {
        def picklist = new Picklist(id: "2345")
        def picklistItem = new PicklistItem(id:"3322")
        def order = new Order(id: "o1")
        mockDomain(Picklist, [picklist])
        mockDomain(PicklistItem, [picklistItem])
        mockForConstraintsTests(Picklist)
        picklist.addToPicklistItems(picklistItem)
        mockDomain(Order, [order])
        picklist.order = order

        def picklistServiceMock = mockFor(PicklistService)
        picklistServiceMock.demand.save { data ->
            picklist.validate()
            picklist
        }
        controller.picklistService = picklistServiceMock.createMock()

        controller.request.contentType = 'text/json'
        controller.request.content ='{"id":"2345"}'

        controller.save()
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)

        assert jsonResponse.success
        picklistServiceMock.verify()
    }
}

