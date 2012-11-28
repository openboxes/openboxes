package org.pih.warehouse

import grails.test.ControllerUnitTestCase
import org.pih.warehouse.product.*
import grails.converters.JSON
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.inventory.*
import org.pih.warehouse.core.*



class JsonControllerTests extends ControllerUnitTestCase {

    void testSearchProductByName() {
        def bostonInventory = new Inventory(id: "bostonInventory")        
        def location = new Location(name: "boston", id: "1234", inventory: bostonInventory)
        mockDomain(Location, [location])
        def group1 = new ProductGroup(id: "group1", description: "painkiller")
        def group2 = new ProductGroup(id: "group2", description: "painLight")
        def product11 = new Product(id: "product11", name: "sophin 2500mg")
        def product12 = new Product(id: "product12", name: "boo killer")
        def product21 = new Product(id: "product21", name: "foo killer")
        def product22 = new Product(id: "product22", name: "moo")
        def product3 = new Product(id: "product3", name: "gookiller")

        def productsSearchResult = [
          [product11.id, product11.name, group1.description, group1.id],
          [product12.id, product12.name, group1.description, group1.id],
          [product21.id, product21.name, group2.description, group2.id],
          [product3.id, product3.name, null, null],
        ]

        def productServiceMock = mockFor(ProductService)
        productServiceMock.demand.searchProductAndProductGroup(1..1){ term -> productsSearchResult}

        def inventoryServiceMock = mockFor(InventoryService)
        def quantities = [:]
        quantities[product11.id] = 1100
        quantities[product12.id] = 1200
        quantities[product21.id] = 2100
        quantities[product22.id] = 2200
        quantities[product3.id] = 3000

        inventoryServiceMock.demand.getQuantityForProducts{inventory, prodcutIds -> quantities}
        controller.inventoryService =  inventoryServiceMock.createMock()
        controller.productService = productServiceMock.createMock()
        controller.session.warehouse = location
        controller.params.term = "killer"
        controller.searchProduct()
        def jsonResponse = controller.response.contentAsString
        def jsonResult = JSON.parse(jsonResponse)

        
        //result should be sorted by group name and then product name
        assert jsonResult[0].id == product3.id
        assert jsonResult[1].id == group2.id
        assert jsonResult[2].id == product21.id
        assert jsonResult[3].id == group1.id
        assert jsonResult[4].id == product12.id
        assert jsonResult[5].id == product11.id

        //result should contain label
        assert jsonResult[0].value == product3.name
        assert jsonResult[1].value == group2.description
        assert jsonResult[2].value == product21.name
        assert jsonResult[3].value == group1.description
        assert jsonResult[4].value == product12.name
        assert jsonResult[5].value == product11.name
        
        //result should contain type
        assert jsonResult[0].type == "Product"
        assert jsonResult[1].type == "ProductGroup"
        assert jsonResult[2].type == "Product"
        assert jsonResult[3].type == "ProductGroup"
        assert jsonResult[4].type == "Product"
        assert jsonResult[5].type == "Product"

        //result should contain quantity
        assert jsonResult[0].quantity == 3000
        assert jsonResult[1].quantity == null
        assert jsonResult[2].quantity == 2100
        assert jsonResult[3].quantity == null
        assert jsonResult[4].quantity == 1200
        assert jsonResult[5].quantity == 1100

        //result should contain group

        assert jsonResult[1].group == ""
        assert jsonResult[2].group == group2.description
        assert jsonResult[3].group == ""
        assert jsonResult[4].group == group1.description
        assert jsonResult[5].group == group1.description

    }

}
