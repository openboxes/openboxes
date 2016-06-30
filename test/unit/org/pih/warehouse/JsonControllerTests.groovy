package org.pih.warehouse

import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.pih.warehouse.product.*
import grails.converters.JSON
import org.pih.warehouse.inventory.*
import org.pih.warehouse.core.*
import org.pih.warehouse.shipping.Shipment


@TestFor(JsonController)
//@Mock([Location, Product, ProductService])
@Build([Location, Product, Inventory])
class JsonControllerTests {

    void testSearchProductByName() {
        def bostonInventory = Inventory.build(id: "bostonInventory")
        def location = Location.build(id: "1234", name: "boston", inventory: bostonInventory)
        def product11 = Product.build(id: "product11", name: "sophin 2500mg", productCode: "ab11")
        def product12 = Product.build(id: "product12", name: "boo killer", productCode: "ab12")
        def product21 = Product.build(id: "product21", name: "foo killer", productCode: "ab21")
        def product22 = Product.build(id: "product22", name: "moo", productCode: "ab22")
        def product3 = Product.build(id: "product3", name: "gookiller", productCode: "ab3")

        def productsSearchResult = [
          [product11.id, product11.name, product11.productCode],
          [product12.id, product12.name, product12.productCode],
          [product21.id, product21.name, product21.productCode],
          [product3.id, product3.name, product3.productCode] ]

        def productServiceMock = mockFor(ProductService)
        productServiceMock.demand.searchProductAndProductGroup { term -> productsSearchResult}
        //productServiceMock.demand.searchProductAndProductGroup { term, useWildcard -> productsSearchResult}
        productServiceMock.demand.getProducts { String[] ids -> [ product11, product12, product21, product3 ]}
        controller.productService = productServiceMock.createMock()

        def quantities = [:]
        quantities[product11.id] = 1100
        quantities[product12.id] = 1200
        quantities[product21.id] = 2100
        quantities[product22.id] = 2200
        quantities[product3.id] = 3000

        def inventoryServiceMock = mockFor(InventoryService)
        inventoryServiceMock.demand.getQuantityForProducts{inventory, productIds -> quantities}
        controller.inventoryService =  inventoryServiceMock.createMock()

        controller.session.warehouse = location
        controller.params.term = "killer"
        controller.searchProduct()
        def jsonResponse = controller.response.contentAsString
        def jsonResult = JSON.parse(jsonResponse)

		
        println jsonResult
		
        //result should be sorted by group name and then product name
        assert jsonResult[0].id == product11.id
        //assert jsonResult[1].id == group2.id
        assert jsonResult[1].id == product12.id
        //assert jsonResult[3].id == group1.id
        assert jsonResult[2].id == product21.id
        assert jsonResult[3].id == product3.id

        //result should contain label
        assert jsonResult[0].value == product11.productCode + " " + product11.name + " (EA/1)"
        //assert jsonResult[1].value == group2.description
        assert jsonResult[1].value == product12.productCode + " " + product12.name + " (EA/1)"
        //assert jsonResult[3].value == group1.description
        assert jsonResult[2].value == product21.productCode + " " + product21.name + " (EA/1)"
        assert jsonResult[3].value == product3.productCode + " " + product3.name + " (EA/1)"
        
        //result should contain type
        assert jsonResult[0].type == "Product"
        //assert jsonResult[1].type == "ProductGroup"
        assert jsonResult[1].type == "Product"
        //assert jsonResult[3].type == "ProductGroup"
        assert jsonResult[2].type == "Product"
        assert jsonResult[3].type == "Product"

        //result should contain quantity
        //assert jsonResult[0].quantity == null //3000
        //assert jsonResult[1].quantity == null
        //assert jsonResult[1].quantity == null //2100
        //assert jsonResult[3].quantity == null
        //assert jsonResult[2].quantity == null //1200
        //assert jsonResult[3].quantity == null //1100

        //result should contain group

        //assert jsonResult[1].group == ""
        //assert jsonResult[2].group == group2.description
        //assert jsonResult[3].group == ""
        //assert jsonResult[4].group == group1.description
        //assert jsonResult[5].group == group1.description
		
    }
	
	// No signature of method: static org.pih.warehouse.core.Person.withCriteria() is applicable for argument types:

	void testGlobalSearch() {
        def bostonInventory = Inventory.build(id: "bostonInventory")
		def location = Location.build(name: "Boston", inventory: bostonInventory)
		mockDomain(Shipment, [new Shipment(name: "Test Shipment")])
		def product1 = Product.build(name: "Test Product 1")
        def product2 = Product.build(name: "Test Product 2")
        def product3 = Product.build(name: "Test Product 3")
        def product4 = Product.build(name: "Test Product 4")
        def product5 = Product.build(name: "Test Product 5")
        def quantityByProductMap = [product1: 1, product2: 2, product3: 3, product4: 4, product5: 5]


		def inventoryServiceMock = mockFor(InventoryService)
		inventoryServiceMock.demand.getProductsByTermsAndCategories{ terms, categories, includeHidden, inventory, max, offset -> 
			return Product.list() 
		}
        inventoryServiceMock.demand.getQuantityByProductMap { inventory, products ->
            return quantityByProductMap
        }
        def stubMessageTagLib = new Expando()
        stubMessageTagLib.message = { message -> return message }
        controller.metaClass.warehouse = stubMessageTagLib;



		//inventoryServiceMock.demand.getQuantityForProducts{inventory, prodcutIds -> quantities}
		controller.inventoryService =  inventoryServiceMock.createMock()
		//controller.productService = productServiceMock.createMock()
		controller.session.warehouse = location
		controller.params.term = "test"
		controller.globalSearch()
		def jsonResponse = controller.response.contentAsString
		println jsonResponse
		def jsonResult = JSON.parse(jsonResponse)
        println jsonResult
        assertNotNull jsonResult
	}

}
