package org.pih.warehouse.product

import grails.test.ControllerUnitTestCase

import org.pih.warehouse.MessageTagLib;
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.springframework.mock.web.MockHttpServletResponse
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.picklist.*
import org.pih.warehouse.product.Product
import grails.converters.JSON
import groovy.xml.MarkupBuilder;

import org.pih.warehouse.core.ActivityCode
import testutils.MockBindDataMixin

@Mixin(MockBindDataMixin)
class ProductControllerTests extends ControllerUnitTestCase{

    protected void setUp(){
        super.setUp()
        mockBindData()
    }

    void testExportShouldRenderProductsAsCsv(){
		
		def category = new Category(id: "123", name: "category 123")
        def product1 = new Product(id:"1234", name: "product 1234", category: category)
        def product2 = new Product(id:"1236", name: "product 1236", category: category)
        mockDomain(Product, [product1, product2])
		mockDomain(Category, [category])
		
		controller.request.format = "csv"
        def model = controller.export()

		def lines = controller.response.contentAsString.split("\n")
		assertEquals lines[0], '"Name","Category","Description","Product Code","Unit of Measure","Manufacturer","Manufacturer Code","Cold Chain","UPC","NDC","Date Created","Date Updated"'
		assertEquals lines[1], '"product 1234","category 123","","","","","","false","","","",""'
		assertEquals lines[2], '"product 1236","category 123","","","","","","false","","","",""'
    }

	/*
	void testSave() {
		def category1 = new Category(id: "root", name: "root")
		def category2 = new Category(id: "123", name: "category 123")
		def product1 = new Product(id:"1234", name: "product 1234", category: category2)
		def product2 = new Product(id:"1236", name: "product 1236", category: category2)
		def location1 = new Location(id: "1", name: "Boston Headquarters")
		mockDomain(Location, [location1])
		mockDomain(Product, [product1, product2])
		mockDomain(Category, [category1, category2])
		mockDomain(Attribute)
		
		def productControl = mockFor(ProductService)
		productControl.demand.getRootCategory(1..1) { return category1 }

		// 	Initialise the service and test the target method.
		this.controller.productService = productControl.createMock()
		
		mockSession['warehouse'] = location1;
		
		controller.request.method = "POST"
		controller.session.warehouse = location1
		
		controller.params["name"] = "new product"
		controller.params["tagsToBeAdded"] = "new tag"
		controller.params["category.id"] = "123"
		
		controller.save()
				
		//assertNotNull product
		println controller?.modelAndView
		println controller?.redirectArgs
				
		assertEquals "showRecordInventory", controller.redirectArgs.action
		//assertNotNull controller?.redirectArgs?.id
		
		def product = Product.findByName("new product")
		println product
	}
	*/
    
}
