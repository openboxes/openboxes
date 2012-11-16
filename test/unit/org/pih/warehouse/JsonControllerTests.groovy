package org.pih.warehouse

import grails.test.ControllerUnitTestCase
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.Product
import grails.converters.JSON
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.core.*



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

        def inventoryServiceMock = mockFor(InventoryService)
        def quantities = [:]
        quantities[product11] = 1100
        quantities[product12] = 1200
        quantities[product21] = 2100
        quantities[product22] = 2200
        quantities[product3] = 3000
        inventoryServiceMock.demand.getProductsQuantityForInventory{inventory -> quantities}
        controller.inventoryService =  inventoryServiceMock.createMock()
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

//Todo: it seems test does not like withCriteria, waiting for solution; by Peter
//   void testSearchPersonByName(){
//      def john = new Person(id:"1",firstName:"john", lastName:"Hoo", email:"jhoo@abc.com")
//      def tom = new Person(id:"2",firstName:"tom", lastName:"Hoo", email:"thoo@abc.com")
//      def kyle = new Person(id:"3",firstName:"kyle", lastName:"Foo", email:"kfoo@abc.com")
//      def stev = new Person(id:"4",firstName:"stev", lastName:"Foo", email:"sfhoo@abc.com")
//      def magan = new Person(id:"5",firstName:"magan", lastName:"Jsonson", email:"mjsonson@abc.com")
//      mockDomain(Person, [john, tom, kyle, stev, magan])
//
//      controller.params.term = "tom hoo"
//      controller.searchPersonByName()
//
//      def jsonResponse = controller.response.contentAsString
//      def json = JSON.parse(jsonResponse)
//      assert json.size() == 2
//      assert json[0].id == john.id
//      assert json[0].value == john.name
//      assert json[0].label == john.name + " " + john.email
//      assert json[1].id == tom.id
//
//   }
}
