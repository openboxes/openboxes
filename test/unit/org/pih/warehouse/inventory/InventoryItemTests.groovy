package org.pih.warehouse.inventory

import grails.test.GrailsUnitTestCase

import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product

class InventoryItemTests extends GrailsUnitTestCase {

	protected void setUp() {
		super.setUp()
		mockDomain(Category)
		mockDomain(Product)
		mockDomain(InventoryItem)
		
		
	}

	protected void tearDown() {
		super.tearDown()
	}
	
    void testToJsonData(){
        def expirationDate = new Date()
		def category = new Category(id: "cat1", name: "new category")
        def product = new Product(id: "prod1", name:"aspin", category: category)
        def item = new InventoryItem(
                id: "1234",
                product: product,
                lotNumber: "ABCD", 
				expirationDate: expirationDate,
                quantity: 1
        )

        mockDomain(InventoryItem, [item])

        Map json = item.toJson()
		
        assert json.inventoryItemId == item.id
        assert json.productId == item.product.id
        assert json.productName == item.product.name
        assert json.lotNumber == item.lotNumber
		assert json.quantityOnHand == 1
		assert json.quantityATP == 1
        assert json.expirationDate == expirationDate.format("MM/dd/yyyy")

		
    }
}
