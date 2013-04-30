package org.pih.warehouse.picklist
import org.pih.warehouse.core.*
import org.pih.warehouse.requisition.*
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
// import org.pih.warehouse.inventory.InventoryService
// import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.*
import grails.test.GrailsUnitTestCase


class PicklistServiceTests extends GrailsUnitTestCase {
     void testCreateNewPicklist(){
        def requisition = new Requisition(id:"requisition1", name:"myTestRequisition")
        def picklistItem1 = [id:"", "requisitionItem.id":"ri1", "inventoryItem.id":"ii1", quantity:100]
        def picklistItem2 = [id:"", "requisitionItem.id":"ri1", "inventoryItem.id":"ii2", quantity:300]
        Person john = new Person(id: "7890")
        Map data = [id:"", "picker.id": john.id,
          "requisition.id": requisition.id,
          picklistItems:[picklistItem1, picklistItem2]]
        mockDomain(Person, [john])
        mockDomain(Requisition, [requisition])
        mockDomain(Picklist, [])
        mockDomain(PicklistItem, [])
        mockDomain(RequisitionItem, [])
        mockDomain(InventoryItem, [])
        
        def service = new PicklistService()

        def picklist = service.save(data)

        def picklistPersisted = Picklist.findByName(requisition.name)


        assert picklist == picklistPersisted
        def items = picklist.picklistItems.toList()
        assert items.size() == 2       

        def item1 = items.find{it.inventoryItem.id == "ii1"}
        def item2 = items.find{it.inventoryItem.id == "ii2"}
        assert item1
        assert item2
        assert item1.requisitionItem.id == "ri1"
        assert item1.quantity == 100
        assert item2.requisitionItem.id == "ri1"
        assert item2.quantity == 300


     }
     void testUpdatePicklist(){
        def requisition = new Requisition(id:"requisition1", name:"myTestRequisition")
        def picklistInDb = new Picklist(id:"picklist1", requisition: requisition)
        def plItem1 = new PicklistItem(id:"pli1", quantity:10, comment:"good")
        def plItem2 = new PicklistItem(id:"pli2", quantity:20, comment: "better")
        mockDomain(Picklist, [picklistInDb])
        mockDomain(PicklistItem, [plItem1, plItem2])

        picklistInDb.addToPicklistItems(plItem1)
        picklistInDb.addToPicklistItems(plItem2)
        Person john = new Person(id: "7890")
        def picklistItem1 = [id: plItem1.id, "requisitionItem.id":"ri1", "inventoryItem.id":"ii1", quantity:100]
        def picklistItem2 = [id:plItem2.id, "requisitionItem.id":"ri1", "inventoryItem.id":"ii2", quantity:300]

        Map data = [id:picklistInDb.id, "picker.id": john.id,
          "requisition.id": requisition.id,
          picklistItems:[picklistItem1, picklistItem2]]
        mockDomain(Person, [john])
        mockDomain(Requisition, [requisition])

        mockDomain(RequisitionItem, [])
        mockDomain(InventoryItem, [])
        
        def service = new PicklistService()

        def picklist = service.save(data)

        def picklistPersisted = Picklist.findByName(requisition.name)


        assert picklist == picklistPersisted
        def items = picklist.picklistItems.toList()
        assert items.size() == 2
        def item1 = items.find{it.id == plItem1.id}
        def item2 = items.find{it.id == plItem2.id}
        assert item1
        assert item2
        assert item1.requisitionItem.id == "ri1"
        assert item1.inventoryItem.id == "ii1"
        assert item1.quantity == 100
        assert item1.comment == plItem1.comment
        assert item2.requisitionItem.id == "ri1"
        assert item2.inventoryItem.id == "ii2"
        assert item2.quantity == 300
        assert item2.comment == plItem2.comment

     }

     void testDeletePicklistItem(){
        def requisition = new Requisition(id:"requisition1", name:"myTestRequisition")
        def picklistInDb = new Picklist(id:"picklist1", requisition: requisition)
        def plItem1 = new PicklistItem(id:"pli1", quantity:10, comment:"good")
        def plItem2 = new PicklistItem(id:"pli2", quantity:20, comment: "better")
        mockDomain(Picklist, [picklistInDb])
        mockDomain(PicklistItem, [plItem1, plItem2])
        picklistInDb.addToPicklistItems(plItem1)
        picklistInDb.addToPicklistItems(plItem2)

        Person john = new Person(id: "7890")
        def picklistItem1 = [id: plItem1.id, "requisitionItem.id":"ri1", "inventoryItem.id":"ii1", quantity:100]

        Map data = [id:picklistInDb.id, "picker.id": john.id,
          "requisition.id": requisition.id,
          picklistItems:[picklistItem1]]
        mockDomain(Person, [john])
        mockDomain(Requisition, [requisition])

        mockDomain(RequisitionItem, [])
        mockDomain(InventoryItem, [])
        
        def service = new PicklistService()

        def picklist = service.save(data)

        def picklistPersisted = Picklist.findByName(requisition.name)

        assert picklist == picklistPersisted
        def items = picklist.picklistItems.toList()
        assert items.size() == 1
        def item1 = items.find{it.id == plItem1.id}
        def item2 = items.find{it.id == plItem2.id}
        assert item1
        assert !item2
     }


     void testCancelPicklist() {
        def requisition = new Requisition(id:"requisition1", name:"myTestRequisition")
        def picklistInDb = new Picklist(id:"picklist1", requisition: requisition)
        def plItem1 = new PicklistItem(id:"pli1", quantity:10, comment:"good")
        def plItem2 = new PicklistItem(id:"pli2", quantity:20, comment: "better")
        mockDomain(Picklist, [picklistInDb])
        mockDomain(PicklistItem, [plItem1, plItem2])
        picklistInDb.addToPicklistItems(plItem1)
        picklistInDb.addToPicklistItems(plItem2)

        mockDomain(RequisitionItem, [])
        mockDomain(InventoryItem, [])
        
     //   def service = new PicklistService()
//
//        def picklistPersisted = Picklist.findByName(requisition.name)
//
//        assert picklist == picklistPersisted
//        def items = picklist.picklistItems.toList()
//        assert items.size() == 1
//        def item1 = items.find{it.id == plItem1.id}
//        def item2 = items.find{it.id == plItem2.id}
//        assert item1
//        assert !item2

     }
}

