package org.pih.warehouse

import grails.test.ControllerUnitTestCase
import org.pih.warehouse.product.*
import grails.converters.JSON
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.inventory.*
import org.pih.warehouse.core.*



class JsonControllerTests extends ControllerUnitTestCase {

    void testSearchProductByName() {
        def bostonInventory = new Inventory(id: "bostonInventory")        
        def location = new Location(name: "boston", id: "1234", inventory: bostonInventory)
        mockDomain(Location, [location])
        def group1 = new ProductGroup(id: "group1", description: "painkiller")
        def group2 = new ProductGroup(id: "group2", description: "painLight")
        def product11 = new Product(id: "product11", name: "sophin 2500mg", productCode: "ab11")
        def product12 = new Product(id: "product12", name: "boo killer", productCode: "ab12")
        def product21 = new Product(id: "product21", name: "foo killer", productCode: "ab21")
        def product22 = new Product(id: "product22", name: "moo", productCode: "ab22")
        def product3 = new Product(id: "product3", name: "gookiller", productCode: "ab3")



        def productsSearchResult = [
          [product11.id, product11.name, product11.productCode],
          [product12.id, product12.name, product12.productCode],
          [product21.id, product21.name, product21.productCode],
          [product3.id, product3.name, product3.productCode] ]

        def productServiceMock = mockFor(ProductService)
        productServiceMock.demand.searchProductAndProductGroup(1..2){ term -> productsSearchResult}
        productServiceMock.demand.getProducts(1..1){ term -> [ product11, product12, product21, product3 ]}

        def inventoryServiceMock = mockFor(InventoryService)
        def quantities = [:]
        quantities[product11.id] = 1100
        quantities[product12.id] = 1200
        quantities[product21.id] = 2100
        quantities[product22.id] = 2200
        quantities[product3.id] = 3000

        inventoryServiceMock.demand.getQuantityForProducts{inventory, productIds -> quantities}
        controller.inventoryService =  inventoryServiceMock.createMock()
        controller.productService = productServiceMock.createMock()
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
	/*
	def test_globalSearch() { 
		def location = new Location(name: "Boston")
		mockDomain(Shipment, [new Shipment(name: "Test Shipment")])
		mockDomain(Product, [new Product(name: "Test Product")])
		def inventoryServiceMock = mockFor(InventoryService)
		inventoryServiceMock.demand.getProductsByTermsAndCategories{ terms, categories, includeHidden, inventory, max, offset -> 
			return Product.list() 
		}
		//inventoryServiceMock.demand.getQuantityForProducts{inventory, prodcutIds -> quantities}
		controller.inventoryService =  inventoryServiceMock.createMock()
		//controller.productService = productServiceMock.createMock()
		controller.session.warehouse = location
		controller.params.term = "test"
		controller.globalSearch()
		def jsonResponse = controller.response.contentAsString
		println jsonResponse
		def jsonResult = JSON.parse(jsonResponse)
	}
	*/
	
}
