package org.pih.warehouse.requisition
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.*
import grails.test.GrailsUnitTestCase


class RequisitionServiceTests extends GrailsUnitTestCase {
    void testCreateNewRequisition(){
        mockDomain(Requisition, [])
        mockDomain(RequisitionItem, [])
        Product product1 = new Product(id:"prod1")
        Product product2 = new Product(id:"prod2")
        mockDomain(Product, [product1,product2])
        Location boston = new Location(name:"boston", id:"2")
        Location miami = new Location(name: "miami", id: "3")
        mockDomain(Location, [boston, miami])
        Person john = new Person(id: "7890")
        mockDomain(Person, [john])
        def today = new Date()
        def tomorrow = new Date().plus(1)
        def items = [
          ["product.id":product1.id, quantity: 300, orderIndex: 0],
          ["product.id":product2.id, quantity: 400, orderIndex: 1]]
        def jsonNull = new org.codehaus.groovy.grails.web.json.JSONObject.Null() 
        Map data = [id: jsonNull, "origin.id": miami.id, "requestedBy.id": john.id, 
        dateRequested: today, requestedDeliveryDate: tomorrow,
        name: "testRequisition", 
        requisitionItems: items]
        def service = new RequisitionService()

        def requisition = service.saveRequisition(data, boston)

        def requisitionPersisted = Requisition.findByName("testRequisition")


        assert requisition == requisitionPersisted
        assert requisition.requestedBy == john
        assert requisition.origin == miami
        assert requisition.destination == boston
        assert requisition.dateRequested == today
        assert requisition.requestedDeliveryDate == tomorrow
        assert requisition.requisitionItems.any{ item -> item.product == product1 && item.quantity == 300 && item.orderIndex == 0}
        assert requisition.requisitionItems.any{ item -> item.product == product2 && item.quantity == 400 && item.orderIndex == 1}
    }
    
  
    void testUpdateRequisition(){
        Product product1 = new Product(id:"prod1")
        Product product2 = new Product(id:"prod2")
        mockDomain(Product, [product1,product2])
        Location boston = new Location(name:"boston", id:"2")
        Location miami = new Location(name: "miami", id: "3")
        Location toronto = new Location(name: "toronto", id: "4")
        mockDomain(Location, [boston, miami, toronto])
        Person john = new Person(id: "7890")
        mockDomain(Person, [john])
        
        def oldItem1 = new RequisitionItem(id:"item1", quantity: 30)
        def oldItem2 = new RequisitionItem(id:"item2", quantity: 40)
        def oldRequisition = new Requisition(id:"requisition1", 
          origin: toronto, name:"oldRequisition",
          description: "oldDescription",
          requisitionItems: [oldItem1, oldItem2])
        mockDomain(Requisition, [oldRequisition])
        mockDomain(RequisitionItem, [oldItem1, oldItem2])

        def today = new Date()
        def tomorrow = new Date().plus(1)
        def items = [
          [id: oldItem1.id, "product.id":product1.id, quantity: 300, orderIndex: 0],
          [id: oldItem2.id, "product.id":product2.id, quantity: 400, orderIndex: 1]]
        Map data = [id: oldRequisition.id, "origin.id": miami.id, "requestedBy.id": john.id, 
        dateRequested: today, requestedDeliveryDate: tomorrow,
        name: "testRequisition", 
        requisitionItems: items]

        def service = new RequisitionService()
        def requisition = service.saveRequisition(data, boston)
        def requisitionPersisted = Requisition.findByName(data.name)
        assert requisitionPersisted
        assert requisition == requisitionPersisted
        assert requisition.id == oldRequisition.id
        assert requisition.description == oldRequisition.description
        assert requisition.requestedBy == john
        assert requisition.origin == miami
        assert requisition.destination == boston
        assert requisition.dateRequested == today
        assert requisition.requestedDeliveryDate == tomorrow
        assert requisition.requisitionItems.size() == 2
        assert requisition.requisitionItems.any{ item -> item.product == product1 && item.quantity == 300 && item.orderIndex == 0}
        assert requisition.requisitionItems.any{ item -> item.product == product2 && item.quantity == 400 && item.orderIndex == 1}
    }

    void testDeleteRequisitionItems(){
        Product product1 = new Product(id:"prod1", name: "prodName1")
        Product product2 = new Product(id:"prod2", name: "prodName2")
        mockDomain(Product, [product1,product2])
        Location boston = new Location(name:"boston", id:"2")
        Location miami = new Location(name: "miami", id: "3")
        Location toronto = new Location(name: "toronto", id: "4")
        mockDomain(Location, [boston, miami, toronto])
        Person john = new Person(id: "7890")
        mockDomain(Person, [john])
        
        def oldItem1 = new RequisitionItem(id:"item1", quantity: 30)
        def oldItem2 = new RequisitionItem(id:"item2", quantity: 40)
        def oldRequisition = new Requisition(id:"requisition1", 
          origin: toronto, name:"oldRequisition",
          description: "oldDescription",
          requisitionItems: [oldItem1, oldItem2])
        mockDomain(Requisition, [oldRequisition])
        mockDomain(RequisitionItem, [oldItem1, oldItem2])

        def today = new Date()
        def tomorrow = new Date().plus(1)
        def items = [
          [id: oldItem2.id, "product.id":product2.id, quantity: 400, orderIndex: 1]]
        Map data = [id: oldRequisition.id, "origin.id": miami.id, "requestedBy.id": john.id, 
        dateRequested: today, requestedDeliveryDate: tomorrow,
        name: "testRequisition", 
        requisitionItems: items]

        def service = new RequisitionService()
        def requisition = service.saveRequisition(data, boston)
        def requisitionPersisted = Requisition.findByName(data.name)
        assert requisitionPersisted
    
        assert requisition == requisitionPersisted
        assert requisition.id == oldRequisition.id
        assert requisition.description == oldRequisition.description
        assert requisition.requestedBy == john
        assert requisition.origin == miami
        assert requisition.destination == boston
        assert requisition.dateRequested == today
        assert requisition.requestedDeliveryDate == tomorrow
        assert requisition.requisitionItems.size() == 1
        assert requisition.requisitionItems.any{ item -> item.product == product2 && item.quantity == 400 && item.orderIndex == 1}
    }
}

