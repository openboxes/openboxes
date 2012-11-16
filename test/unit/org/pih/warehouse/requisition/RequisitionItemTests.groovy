package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.inventory.InventoryItem
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

    void testToJsonData(){
      def product = new Product(id: "prod1", name:"aspin")
      def item = new RequisitionItem(
        id: "1234",
        version: 5,
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
      assert json.version == 5
    }

    void testToJsonWithInventoryItemsData() {
        def product = new Product(id: "prod1", name:"asprin")
        def inventoryItem = new InventoryItem(id: "inventoryItem1", product: product, lotNumber: "abcd")
        mockDomain(Product, [product])
        mockDomain(InventoryItem, [inventoryItem]);
        def item = new RequisitionItem(
                id: "1234",
                product: product,
                quantity: 3000,
                comment: "good",
                recipient: "peter",
                substitutable: true,
                orderIndex: 3
        )
        mockDomain(RequisitionItem, [item])

        def result = item.toJsonIncludeInventoryItems();
        assert result.getClass() == LinkedHashMap
        assert result.inventoryItems

        assert result.inventoryItems.getClass() == ArrayList
        assert result.inventoryItems[0].id == inventoryItem.id
        assert result.inventoryItems[0].productId == inventoryItem.product.id
        assert result.inventoryItems[0].productName == inventoryItem.product.name


    }
}
