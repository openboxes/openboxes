package org.pih.warehouse.picklist
import org.pih.warehouse.core.*
import org.pih.warehouse.requisition.*
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
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
        assert items.first().requisitionItem.id == "ri1"
        assert items.first().inventoryItem.id == "ii1"
        assert items.first().quantity == 100
        assert items.last().requisitionItem.id == "ri1"
        assert items.last().inventoryItem.id == "ii2"
        assert items.last().quantity == 300

     }
}

