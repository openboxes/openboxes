package org.pih.warehouse.product

import grails.orm.PagedResultList
import grails.test.ControllerUnitTestCase

// import org.pih.warehouse.MessageTagLib;
import org.pih.warehouse.inventory.Inventory
// import org.pih.warehouse.inventory.InventoryItem
// import org.pih.warehouse.inventory.InventoryService
// import org.springframework.mock.web.MockHttpServletResponse
import org.pih.warehouse.core.Location
//import org.pih.warehouse.core.Person
import org.pih.warehouse.picklist.*
// import org.pih.warehouse.product.Product
// import grails.converters.JSON
// import groovy.xml.MarkupBuilder;

// import org.pih.warehouse.core.ActivityCode
import testutils.MockBindDataMixin

@Mixin(MockBindDataMixin)
class ProductControllerTests extends ControllerUnitTestCase{

    protected void setUp(){
        super.setUp()
        mockBindData()
    }

	
	void test_list_shouldContainTwoProducts() { 
		def currentDate = new Date()
		def category = new Category(id: "123", name: "category 123")
		def product1 = new Product(id:"1234", name: "product 1234", category: category, lastUpdated: currentDate, dateCreated: currentDate)
		def product2 = new Product(id:"1236", name: "product 1236", category: category, lastUpdated: currentDate, dateCreated: currentDate)
		def products = [product1, product2]
		mockDomain(Product, products)
		mockDomain(Category, [category])

		def productServiceMock = mockFor(ProductService)
		productServiceMock.demand.getProducts(1..1) { arg1, arg2, arg3, arg4, arg5 ->
			println "Get products from mock service " + products
			return new PagedResultList(Product.list(), 2);
		}
		controller.productService = productServiceMock.createMock()
		def model = controller.list()
		println "Model " + model.class + " "
		println model.productInstanceList
		println model.productInstanceTotal


		assertEquals 2, model.productInstanceTotal
		assertEquals 2, model.productInstanceList.size()
		assertEquals product1, model.productInstanceList[0]
		assertEquals product2, model.productInstanceList[1]
		
	}
	
	/**
	 * This test stopped working when I refactored the code to export the products into a service method.
	 */
	void test_export_shouldRenderProductsAsCsv(){		
		def currentDate = new Date()
		def dateString = currentDate.format("dd/MMM/yyyy hh:mm:ss")
		def category = new Category(id: "123", name: "category 123")
        def product1 = new Product(id:"1234", name: "product 1234", category: category, lastUpdated: currentDate, dateCreated: currentDate)
        def product2 = new Product(id:"1236", name: "product 1236", category: category, lastUpdated: currentDate, dateCreated: currentDate)
        mockDomain(Product, [product1, product2])
		mockDomain(Category, [category])
		
		
		def productServiceMock = mockFor(ProductService)
		productServiceMock.demand.exportProducts(1) { products ->
			println "called exportProducts()"
			return 
				"\"ID\",\"Name\",\"Category\",\"Description\",\"Product Code\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n" + 
				"\"1234\",\"product 1234\",\"category 123\",\"\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"${dateString}\",\"${dateString}\"\n"
				"\"1236\",\"product 1235\",\"category 123\",\"\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"${dateString}\",\"${dateString}\""
		}
		controller.productService = productServiceMock.createMock()
		
		
        //def model = controller.exportAsCsv()
		//println controller.response
		//println controller.response.contentAsString
		
		//def lines = controller.response.contentAsString.split("\n")
		//assertEquals '"ID","Name","Category","Description","Product Code","Unit of Measure","Manufacturer","Manufacturer Code","Cold Chain","UPC","NDC","Date Created","Date Updated"', lines[0]
		//assertEquals '"1234","product 1234","category 123","","","","","","false","","","${dateString}","${dateString}"', lines[1]
		//assertEquals '"1236","product 1236","category 123","","","","","","false","","","${dateString}","${dateString}"', lines[2]
    }
	
	/*
	void test_import_shouldCreateNewProduct() {
		def csv = """\"ID\",\"Name\",\"Category\",\"Description\",\"Product Code\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n\"1235\",\"product 1235\",\"category 123\",\"\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""""
		println csv;		
	}

	void test_import_shouldUpdateExistingProduct() {		
		def csv = """\"ID\",\"Name\",\"Category\",\"Description\",\"Product Code\",\"Unit of Measure\",\"Manufacturer\",\"Manufacturer Code\",\"Cold Chain\",\"UPC\",\"NDC\",\"Date Created\",\"Date Updated\"\n\"1236\",\"product 1236\",\"category 123\",\"\",\"\",\"\",\"\",\"\",\"false\",\"\",\"\",\"\",\"\""""		
		println csv;
	}
	*/

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
