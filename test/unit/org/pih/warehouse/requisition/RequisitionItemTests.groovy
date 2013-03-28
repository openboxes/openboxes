package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase

import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService;
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class RequisitionItemTests extends GrailsUnitTestCase {

    void testNotNullableConstraints() {
        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem(quantity: null)
        assertFalse requisitionItem.validate()
        assertEquals "nullable", requisitionItem.errors["product"]
        assertEquals "nullable", requisitionItem.errors["quantity"]
    }

    void testQuantityConstraint() {
        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem(quantity: 0)
        assertFalse requisitionItem.validate()
        println requisitionItem.errors["quantity"]
    }

    void testToJsonData(){
      def product = new Product(id: "prod1", name:"aspin")
      def requisitionItem = new RequisitionItem(
        id: "1234",
        product: product,
        quantity: 3000,
        comment: "good",
        recipient: "peter",
        substitutable: true,
        orderIndex: 3
      )
	  
	  mockDomain(Product, [product])
	  mockDomain(RequisitionItem, [requisitionItem])
	  
      Map json = requisitionItem.toJson()	 
	  
	  println json 
      assert json.id == requisitionItem.id
      assert json.productId == requisitionItem.product.id
      assert json.productName == requisitionItem.product.name + " (EA/1)"
      assert json.quantity == requisitionItem.quantity
      assert json.comment == requisitionItem.comment
      assert json.recipient == requisitionItem.recipient
      assert json.substitutable
      assert json.orderIndex == requisitionItem.orderIndex
    }

    void testcalculateQuantityPicked() {

        def requisitionItem = new RequisitionItem(id: "reqItem1")
        mockDomain(RequisitionItem, [requisitionItem])

        def picklistItem1 = new PicklistItem(id: "pickItem1", quantity: 30)
        def picklistItem2 = new PicklistItem(id: "pickItem2", quantity: 100)
        def picklistItem3 = new PicklistItem(id: "pickItem3", quantity: 205)
        mockDomain(PicklistItem, [picklistItem1, picklistItem2, picklistItem3])

        picklistItem1.requisitionItem = requisitionItem
        picklistItem2.requisitionItem = requisitionItem
        picklistItem3.requisitionItem = requisitionItem

        assert requisitionItem.calculateQuantityPicked() == (30 + 100 + 205)

    }

    void testcalcuateNumInventoryItem()
    {
        def inventoryItem1 = new InventoryItem(id: "invent1")
        def inventoryItem2 = new InventoryItem(id: "invent2")
        def inventoryItem3 = new InventoryItem(id: "invent3")
        mockDomain(InventoryItem, [inventoryItem1, inventoryItem2, inventoryItem3])

        def product = new Product(id: "prod1")
        mockDomain(Product, [product])

        inventoryItem1.product = product
        inventoryItem2.product = product
        inventoryItem3.product = product

        def requisitionItem = new RequisitionItem(id: "reqItem1")
        mockDomain(RequisitionItem, [requisitionItem])

        requisitionItem.product = product

        assert requisitionItem.calculateNumInventoryItem() == 3
    }

    void testRetrievePicklistItems() {
        def requisitionItem = new RequisitionItem(id: "reqItem1")
        def picklistItem1 = new PicklistItem(id: "pickItem1", requisitionItem: requisitionItem, quantity: 30)
        def picklistItem2 = new PicklistItem(id: "pickItem2", requisitionItem: requisitionItem, quantity: 50)
        def picklistItem3 = new PicklistItem(id: "pickItem3", requisitionItem: requisitionItem, quantity: 60)
        mockDomain(RequisitionItem, [requisitionItem])
        mockDomain(PicklistItem, [picklistItem1, picklistItem2, picklistItem3])

        def list = requisitionItem.retrievePicklistItems()
        assert list.contains(picklistItem1)
        assert list.contains(picklistItem2)
        assert list.contains(picklistItem3)

    }
}
