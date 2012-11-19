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


    void testIsDefaultValue(){

        assert new RequisitionItem().checkIsEmpty()
        assert new RequisitionItem(orderIndex: 45).checkIsEmpty()  //ignore order index
        assert new RequisitionItem(id: "").checkIsEmpty()  //ignore order index
        assert new RequisitionItem(comment: "").checkIsEmpty()  //ignore order index
        assert new RequisitionItem(recipient: "").checkIsEmpty()  //ignore order index
        assertFalse new RequisitionItem(id: "abc").checkIsEmpty()
        assertFalse new RequisitionItem(product: new Product()).checkIsEmpty()
        assertFalse new RequisitionItem(quantity: 23).checkIsEmpty()
        assertFalse new RequisitionItem(substitutable: true).checkIsEmpty()
        assertFalse new RequisitionItem(comment: "hi").checkIsEmpty()
        assertFalse new RequisitionItem(recipient: "zhao").checkIsEmpty()
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
      assert json.productName == requisitionItem.product.name
      assert json.quantity == requisitionItem.quantity
      assert json.comment == requisitionItem.comment
      assert json.recipient == requisitionItem.recipient
      assert json.substitutable
      assert json.orderIndex == requisitionItem.orderIndex
    }
}
