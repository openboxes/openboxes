package org.pih.warehouse.picklist

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem

class PicklistItemTests extends GrailsUnitTestCase
{
    void testNotNullableConstraints() {
        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem(quantity: null)
        assertFalse requisitionItem.validate()
        assertEquals "nullable", requisitionItem.errors["quantity"]
    }

    void testToJsonData(){
        def product = new Product(id: "prod1", name:"aspin")
        def inventoryItem = new InventoryItem(
                id: "inventoryItem1",
                product: product,
                lotNumber: "ABCDEFG"
        )
        def requisitionItem = new RequisitionItem(
                id: "1234",
                product: product,
                quantity: 3000,
                comment: "good",
                recipient: new Person(firstName: "peter", lastName: "zhao")
        )

        def picklistItem = new PicklistItem(
                id: "15131",
                requisitionItem: requisitionItem,
                inventoryItem: inventoryItem,
                quantity: 20,
        )
        Map json = picklistItem.toJson()
        assert json.id == picklistItem.id
        assert json.requisitionItemId == picklistItem.requisitionItem.id
        assert json.inventoryItemId == picklistItem.inventoryItem.id
        assert json.quantity == picklistItem.quantity
    }

}
