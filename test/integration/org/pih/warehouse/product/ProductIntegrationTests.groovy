package org.pih.warehouse.product
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.*
import testutils.DbHelper

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

    void testLatestInventoryDate(){
      def product = DbHelper.creatProductIfNotExist("TestProductABC") 
      Location boston =  Location.findByName("Boston Headquarters")
      Location miami =  Location.findByName("Miami Warehouse");
      def tenDaysAgo = new Date().minus(10)
      def fiveDaysAgo = new Date().minus(5)
      def threeDaysAgo = new Date().minus(3)
      def sevenDaysAgo = new Date().minus(7)
      DbHelper.recordInventory(product, boston, "tets1234234", new Date().plus(100), 300, tenDaysAgo)
      DbHelper.recordInventory(product, boston, "tets1234234", new Date().plus(100), 300, sevenDaysAgo)
      DbHelper.recordInventory(product, miami, "tets12323412", new Date().plus(100), 300, fiveDaysAgo)
      DbHelper.transferStock(product, boston, "tets1234234", 999, threeDaysAgo, miami)

      def dateForBoston = product.latestInventoryDate(boston.id)
      def dateForMiami = product.latestInventoryDate(miami.id)
      assert dateForBoston.format("MM/dd/yyyy") == sevenDaysAgo.format("MM/dd/yyyy")
      assert dateForMiami.format("MM/dd/yyyy") == fiveDaysAgo.format("MM/dd/yyyy")

    }
}
