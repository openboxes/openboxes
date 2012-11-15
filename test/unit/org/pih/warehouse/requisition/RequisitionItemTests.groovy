package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.picklist.PicklistItem
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

    void testGetQuantityPickedWhenNoPickListItem() {
        def requisitionItem = new RequisitionItem()
        assertEquals 0, requisitionItem.getQuantityPicked()
    }

    void testGetQuantityPickedWhenSingleItemWithZeroQuantity() {
        def requisitionItem = new RequisitionItem()
        mockDomain(RequisitionItem, [requisitionItem])
        def picklistItem = new PicklistItem(quantity: 0)
        requisitionItem.addToPicklistItems(picklistItem)
        assertEquals 0, requisitionItem.getQuantityPicked()
    }

    void testGetQuantityPickedMultiplePickListItem() {
        def requisitionItem = new RequisitionItem()
        mockDomain(RequisitionItem, [requisitionItem])
        requisitionItem.addToPicklistItems(new PicklistItem(quantity: 2000))
        requisitionItem.addToPicklistItems(new PicklistItem(quantity: 3000))
        assertEquals 5000, requisitionItem.getQuantityPicked()
    }


    void testGetQuantityRemaining() {
        def requisitionItem = new RequisitionItem(quantity: 5000)
        mockDomain(RequisitionItem, [requisitionItem])
        requisitionItem.addToPicklistItems(new PicklistItem(quantity: 2000))
        assertEquals 3000, requisitionItem.getQuantityRemaining()
    }

    void testGetStatusWhenNoItemsArePicked() {
        def requisitionItem = new RequisitionItem(quantity: 5000)
        assertEquals "Incomplete", requisitionItem.getStatus()
    }

    void testGetStatusWhenPickedQuantityIsNonZeroLessThanRequested() {
        def requisitionItem = new RequisitionItem(quantity: 5000)
        mockDomain(RequisitionItem, [requisitionItem])
        def picklistItem = new PicklistItem(quantity: 2000)
        requisitionItem.addToPicklistItems(picklistItem)
        assertEquals "PartiallyComplete", requisitionItem.getStatus()
    }

    void testGetStatusWhenPickedQuantityIsEqualToRequested() {
        def requisitionItem = new RequisitionItem(quantity: 5000)
        mockDomain(RequisitionItem, [requisitionItem])
        def picklistItem = new PicklistItem(quantity: 5000)
        requisitionItem.addToPicklistItems(picklistItem)
        assertEquals "Complete", requisitionItem.getStatus()
    }

    void testToJsonData(){
      def product = new Product(id: "prod1", name:"aspin")
      def item = new RequisitionItem(
        id: "1234",
        product: product,
        quantity: 3000,
        comment: "good",
        recipient: "peter",
        substitutable: true,
        orderIndex: 3
      )
      Map json = item.toJson()
      assert json.id == item.id
      assert json.productId == item.product.id
      assert json.productName == item.product.name
      assert json.quantity == item.quantity
      assert json.comment == item.comment
      assert json.recipient == item.recipient
      assert json.substitutable
      assert json.orderIndex == item.orderIndex
    }
}
