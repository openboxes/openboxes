package org.pih.warehouse

import grails.test.ControllerUnitTestCase
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.product.Product
import grails.converters.JSON



class JsonControllerTests extends ControllerUnitTestCase {




    void testSearchProductByName() {
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

       def expectedResults =  '[{"id":"product3","name":"gookiller","type":"Product"},{"id":"group2","name":"painLight","type":"ProductGroup","products":[{"id":"product21","name":"foo killer"}]},{"id":"group1","name":"painkiller","type":"ProductGroup","products":[{"id":"product12","name":"boo killer"},{"id":"product11","name":"sophin 2500mg"}]}]'

        controller.params.term = "killer"
        controller.searchProduct()
        def jsonResponse = controller.response.contentAsString

        assert jsonResponse == expectedResults

    }
}
