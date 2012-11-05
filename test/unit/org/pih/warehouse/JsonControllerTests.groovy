package org.pih.warehouse

import grails.test.ControllerUnitTestCase
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.Product
import grails.converters.JSON
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.core.Location

class InventoryServiceStub{ //use stub because mockFor do not support multiple demands well
   def calculateQuantityForProduct(product, location){
       product.name + "_quantity"
   }
}

class JsonControllerTests extends ControllerUnitTestCase {

    void testSearchProductByName() {
        def location = new Location(name: "boston", id: "1234")
        mockDomain(Location, [location])
        def group1 = new ProductGroup(id: "group1", description: "painkiller")
        def group2 = new ProductGroup(id: "group2", description: "painLight")
        def product11 = new Product(id: "product11", name: "sophin 2500mg")
        def product12 = new Product(id: "product12", name: "boo killer")
        def product21 = new Product(id: "product21", name: "foo killer")
        def product22 = new Product(id: "product22", name: "moo")
        def product3 = new Product(id: "product3", name: "gookiller")
        mockDomain(ProductGroup, [group1, group2])
        mockDomain(Product, [product11, product12, product21, product22, product3])
        group1.addToProducts(product11)
        group1.addToProducts(product12)
        group2.addToProducts(product21)
        group2.addToProducts(product22)
        product11.addToProductGroups(group1)
        product12.addToProductGroups(group1)
        product21.addToProductGroups(group2)
        product22.addToProductGroups(group2)


        controller.inventoryService =  new InventoryServiceStub()
        controller.session.warehouse = location
        controller.params.term = "killer"
        controller.searchProduct()
        def jsonResponse = controller.response.contentAsString
        def jsonResult = JSON.parse(jsonResponse)

        //result should be sorted by group name and then product name
        assert jsonResult[0].value == product3.id
        assert jsonResult[1].value == group2.id
        assert jsonResult[2].value == product21.id
        assert jsonResult[3].value == group1.id
        assert jsonResult[4].value == product12.id
        assert jsonResult[5].value == product11.id

        //result should contain label
        assert jsonResult[0].label == product3.name
        assert jsonResult[1].label == group2.description
        assert jsonResult[2].label == product21.name
        assert jsonResult[3].label == group1.description
        assert jsonResult[4].label == product12.name
        assert jsonResult[5].label == product11.name
        
        //result should contain type
        assert jsonResult[0].type == "Product"
        assert jsonResult[1].type == "ProductGroup"
        assert jsonResult[2].type == "Product"
        assert jsonResult[3].type == "ProductGroup"
        assert jsonResult[4].type == "Product"
        assert jsonResult[5].type == "Product"

        //result should contain quantity
        assert jsonResult[0].quantity == product3.name + "_quantity"
        assert jsonResult[1].quantity == null
        assert jsonResult[2].quantity == product21.name + "_quantity"
        assert jsonResult[3].quantity == null
        assert jsonResult[4].quantity == product12.name + "_quantity"
        assert jsonResult[5].quantity == product11.name + "_quantity"

    }
}
