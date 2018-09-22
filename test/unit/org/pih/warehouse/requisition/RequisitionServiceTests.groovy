package org.pih.warehouse.requisition

import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.*
// import org.pih.warehouse.inventory.Inventory
// import org.pih.warehouse.inventory.InventoryItem
// import org.pih.warehouse.inventory.InventoryService
// import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.*
import grails.test.GrailsUnitTestCase


class RequisitionServiceTests extends GrailsUnitTestCase {

	def requisitionService = new RequisitionService()
	
	
	protected void tearDown() {
		super.tearDown()
	}

    @Ignore
    void createNewRequisitionFromTemplate() {
        fail("Not implemented yet")
    }


    @Ignore
    void issueRequisition_shouldCreateNewTransactions() {
        fail("Not implemented yet")
    }


    void testCreateNewRequisition(){
		requisitionService.identifierService = [generateRequisitionIdentifier: { -> return "ABC-123" }]
		mockDomain(Requisition, [])
		mockDomain(RequisitionItem, [])
		Product product1 = new Product(id:"prod1", name: "Product")
		Product product2 = new Product(id:"prod2", name: "Product")
		mockDomain(Product, [product1, product2])
		Location boston = new Location(name:"boston", id:"2")
		Location miami = new Location(name: "miami", id: "3")
		mockDomain(Location, [boston, miami])
		Person john = new Person(id: "7890")
		mockDomain(Person, [john])
		def today = new Date()
		def tomorrow = new Date().plus(1)
		def items = [
			["product.id":product1.id, quantity: 300, orderIndex: 0],
			["product.id":product2.id, quantity: 400, orderIndex: 1]
		]
		def jsonNull = new org.codehaus.groovy.grails.web.json.JSONObject.Null()
		Map data = [id: jsonNull, "destination.id": miami.id, "requestedBy.id": john.id,
			dateRequested: today, requestedDeliveryDate: tomorrow, commodityClass: CommodityClass.MEDICATION,
			name: "testRequisition", requisitionItems: items]
		//def service = new RequisitionService()

		def requisition = requisitionService.saveRequisition(data, boston)

		def requisitionPersisted = Requisition.findByName("testRequisition")

        assertNotNull requisition
        assertNotNull requisitionPersisted

		assert requisition == requisitionPersisted
		assert requisition.requestedBy == john
		assert requisition.origin == boston
		assert requisition.status == RequisitionStatus.CREATED
		assert requisition.destination == miami
		assert requisition.dateRequested == today
		assert requisition.requestedDeliveryDate == tomorrow
		assert requisition.requisitionItems.any{ item -> item.product == product1 && item.quantity == 300 && item.orderIndex == 0}
		assert requisition.requisitionItems.any{ item -> item.product == product2 && item.quantity == 400 && item.orderIndex == 1}
	}


	void testUpdateRequisition(){
		requisitionService.identifierService = [generateRequisitionIdentifier: { -> return "ABC-123" }]
		
		Product product1 = new Product(id:"prod1")
		Product product2 = new Product(id:"prod2")
		mockDomain(Product, [product1, product2])
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
		status: RequisitionStatus.CREATED,
		description: "oldDescription",
		requisitionItems: [oldItem1, oldItem2])
		mockDomain(Requisition, [oldRequisition])
		mockDomain(RequisitionItem, [oldItem1, oldItem2])

		def today = new Date()
		def tomorrow = new Date().plus(1)
		def items = [
			[id: oldItem1.id, "product.id":product1.id, quantity: 300, orderIndex: 0],
			[id: oldItem2.id, "product.id":product2.id, quantity: 400, orderIndex: 1]
		]
		Map data = [id: oldRequisition.id, "destination.id": miami.id, "requestedBy.id": john.id,
			dateRequested: today, requestedDeliveryDate: tomorrow,
			name: "testRequisition",
			requisitionItems: items]

		//def service = new RequisitionService()
		def requisition = requisitionService.saveRequisition(data, boston)
		def requisitionPersisted = Requisition.findByName(data.name)
		assert requisitionPersisted
		assert requisition == requisitionPersisted
		assert requisition.id == oldRequisition.id
		assert requisition.status == oldRequisition.status
		assert requisition.description == oldRequisition.description
		assert requisition.requestedBy == john
		assert requisition.origin == boston
		assert requisition.destination == miami
		assert requisition.dateRequested == today
		assert requisition.requestedDeliveryDate == tomorrow
		assert requisition.requisitionItems.size() == 2
		assert requisition.requisitionItems.any{ item -> item.product == product1 && item.quantity == 300 && item.orderIndex == 0}
		assert requisition.requisitionItems.any{ item -> item.product == product2 && item.quantity == 400 && item.orderIndex == 1}
	}

	
	void testDeleteRequisitionItems(){
		requisitionService.identifierService = [generateRequisitionIdentifier: { -> return "ABC-123" }]

		Product product1 = new Product(id:"prod1", name: "prodName1")
		Product product2 = new Product(id:"prod2", name: "prodName2")
		mockDomain(Product, [product1, product2])
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
			[id: oldItem2.id, "product.id":product2.id, quantity: 400, orderIndex: 1]
		]
		Map data = [id: oldRequisition.id, "destination.id": miami.id, "requestedBy.id": john.id,
			dateRequested: today, requestedDeliveryDate: tomorrow,
			name: "testRequisition",
			requisitionItems: items]

		//def service = new RequisitionService()
		def requisition = requisitionService.saveRequisition(data, boston)
		def requisitionPersisted = Requisition.findByName(data.name)
		assert requisitionPersisted

		assert requisition == requisitionPersisted
		assert requisition.id == oldRequisition.id
		assert requisition.description == oldRequisition.description
		assert requisition.requestedBy == john
		assert requisition.origin == boston
		assert requisition.destination == miami
		assert requisition.dateRequested == today
		assert requisition.requestedDeliveryDate == tomorrow
		assert requisition.requisitionItems.size() == 1
		assert requisition.requisitionItems.any{ item -> item.product == product2 && item.quantity == 400 && item.orderIndex == 1}
	}

	void testDeleteRequisitionByAdmin() {
		Location toronto = new Location(name: "toronto", id: "4")
		mockDomain(Location, [toronto])

		def requisitionItem1 = new RequisitionItem(id:"item1", quantity: 30)
		def requisitionItem2 = new RequisitionItem(id:"item2", quantity: 40)
		def requisition = new Requisition(id:"requisition1",
		origin: toronto, name:"oldRequisition",
		description: "oldDescription",
		requisitionItems: [
			requisitionItem1,
			requisitionItem2
		])
		mockDomain(Requisition, [requisition])
		mockDomain(RequisitionItem, [
			requisitionItem1,
			requisitionItem2
		])

		//def service = new RequisitionService()
		requisitionService.deleteRequisition(requisition)
		def requisitionFromDb = Requisition.get(requisition.id)
		assert !requisitionFromDb

		def requisitionItemsFromDb = RequisitionItem.findAllByRequisition(requisition)
		assert requisitionItemsFromDb.size() == 0
	}

	void testCancelExistingRequisitionDuringEdit() {
		Location toronto = new Location(name: "toronto", id: "4")
		mockDomain(Location, [toronto])

		def requisition = new Requisition(id:"requisition1",
		origin: toronto, name:"oldRequisition",
		description: "oldDescription")
		mockDomain(Requisition, [requisition])

		//def service = new RequisitionService()
		requisitionService.cancelRequisition(requisition)
		def requisitionFromDb = Requisition.get(requisition.id)

		assert requisitionFromDb.status == RequisitionStatus.CANCELED
	}


}

