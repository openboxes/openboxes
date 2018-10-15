/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.product

import grails.test.*
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.inventory.InventoryItem


class ProductTests extends GrailsUnitTestCase {


    protected void setUp() {
        super.setUp()
        Product product1 = new Product(id: "prod1", name: "product1")
        Product product2 = new Product(id: "prod2", name: "product2")
        Product product3 = new Product(id: "prod3", name: "product3")
        Product product4 = new Product(id: "prod4", name: "product4")


        InventoryItem item1 = new InventoryItem(id: "item1", product:product1)
        InventoryItem item2 = new InventoryItem(id: "item2", product:product2)
        InventoryItem item3 = new InventoryItem(id: "item3", product:product3)
        InventoryItem item4 = new InventoryItem(id: "item4", product:product4)

        product1.inventoryItems = [item1, item2, item3, item4]

        ProductGroup productGroup1 = new ProductGroup(name: "productGroup1")

        ProductGroup productGroup2 = new ProductGroup(name: "productGroup2")

        mockDomain(Product, [product1,product2,product3,product4])
        mockDomain(InventoryItem, [item1, item2, item3, item4])
        mockDomain(ProductGroup, [productGroup1, productGroup2])
        mockDomain(Synonym)
        mockDomain(Category)

        productGroup1.addToProducts(product1)
        productGroup1.addToProducts(product2)
        productGroup1.addToProducts(product4)

        productGroup2.addToProducts(product1)
        productGroup2.addToProducts(product3)
        productGroup2.addToProducts(product4)

        product1.addToProductGroups(productGroup1)
        product1.addToProductGroups(productGroup2)
        product2.addToProductGroups(productGroup1)
        product3.addToProductGroups(productGroup2)
        product4.addToProductGroups(productGroup1)
        product4.addToProductGroups(productGroup2)
    }

    protected void tearDown() {
        super.tearDown()
    }

    @Test
    void toJson() {
        def product = Product.findById("prod1")
        def map = product.toJson()
        assert map.id == product.id
        assert map.name == product.name
//        assert map.inventoryItems.any { it.inventoryItemId == item1.id }
//        assert map.inventoryItems.any { it.inventoryItemId == item2.id }
//        assert map.inventoryItems.any { it.inventoryItemId == item3.id }
//        assert map.inventoryItems.any { it.inventoryItemId == item4.id }

    }

    @Test
    void addToSynonyms_shouldAddNewSynonym() {
        def product = new Product(name: "Product 1")
        product.addToSynonyms(new Synonym(synonym: "synonym"))
        product.save(flush: true)
        assertEquals 1, product.synonyms.size()
    }


    @Ignore
    void alternativeProducts_shouldReturnAlternativeProducts() {
        def product1 = Product.findByName("product1")
        def product2 = Product.findByName("product2")
        def product3 = Product.findByName("product3")
        def product4 = Product.findByName("product4")

        def productGroup1 = ProductGroup.findByName("productGroup1")
        assert productGroup1.products.size() == 3

        def productGroup2 = ProductGroup.findByName("productGroup2")
        assert productGroup2.products.size() == 3

        assert product1.alternativeProducts().size() == 3
        assert !product1.alternativeProducts().contains(product1)
        assert product1.alternativeProducts().contains(product2)
        assert product1.alternativeProducts().contains(product3)
        assert product1.alternativeProducts().contains(product4)

    }


}
