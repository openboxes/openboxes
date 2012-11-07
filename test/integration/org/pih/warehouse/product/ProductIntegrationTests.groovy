package org.pih.warehouse.product

class ProductIntegrationTests extends GroovyTestCase{
    void testSaveProductProductGroup(){
        def suppliers = Category.findByName("Supplies")
        def name = "Test" + UUID.randomUUID().toString()[0..5]
        def group = new ProductGroup(description: name + "group", category: suppliers)
        assert group.save(flush:true, failOnError:true)
        def product = new Product(name: name, category: suppliers)
        product.addToProductGroups(group)
        assert product.save(flush:true, failOnError:true)

        group.addToProducts(product)
        assert group.save(flush:true, failOnError:true)
        assert product.productGroups.contains(group)
        assert group.products.contains(product)


    }



    void testGetProductFromGroup(){
       def group = ProductGroup.findByDescription("PainKiller")
        def products = group.products
        assert products.any{p -> p.name == "Advil 200mg"}
        assert products.any{p -> p.name == "Tylenol 325mg"}

    }

    void testGetProductGroup(){
        def product = Product.findByName("MacBook Pro 8G")
        def groups = product.productGroups
        assert groups.any{g -> g.description == "Laptop"}
    }
}
