package org.pih.warehouse.product

class ProductIntegrationTests extends GroovyTestCase{
    void testSaveProductProductGroup(){
        def suppliers = Category.findByName("Supplies")
        def name = "Test" + UUID.randomUUID().toString()[0..5]

        def product = new Product(name: name, category: suppliers)
        assert product.save(flush:true, failOnError:true)
        def group = new ProductGroup(description: name + "group", category: suppliers)
        group.addToProducts(product)
        assert group.save(flush:true, failOnError:true)

        assert group.products.size() > 0

    }



    void testGetProductFromGroup(){
       def group = ProductGroup.findByDescription("PainKiller")
        def products = group.products
        assert products.any{p -> p.name == "Advil 200mg"}
    }

    void testGetProductGroup(){
        def product = Product.findByName("MacBook Pro 8G")
        def groups = product.productGroups
        assert groups.any{g -> g.description == "Laptop"}
    }
}
