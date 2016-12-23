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
import grails.test.mixin.integration.Integration
import groovy.sql.Sql
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test;

// import org.hibernate.ObjectDeletedException;
// import org.hibernate.ObjectNotFoundException;
// import org.pih.warehouse.inventory.InventoryItem
// import org.springframework.dao.InvalidDataAccessApiUsageException;

@Integration
class ProductGroupIntegrationTests {
	
	def dataSource

    @Before
	void setUp() {

		def category = new Category(name: "Medicines")
		category.save(flush:true)
		
		def product = new Product(name: "Ibuprofen, 200 mg, tablet", category: category)
		product.save(failOnError:true)
		
		def productGroup = new ProductGroup(description:"Ibuprofen", category: category)
		productGroup.save(flush:true)
		
		product.addToProductGroups(productGroup)
		product.save(flush:true,failOnError:true)
		
		def product2 = new Product(name: "Tylenol, 325 mg, tablet", category: category)
		product2.save(failOnError:true)
				
		def productGroup2 = new ProductGroup(description:"Tylenol", category: category)
		productGroup2.save(flush:true)

		productGroup2.addToProducts(product2)
		productGroup2.save(flush:true,failOnError:true)

	}

    @After
	void tearDown() {

	}


    @Test
	void testProductGroupHasManyProducts() {
		
		def productGroup = ProductGroup.findByDescription("Ibuprofen")
		println productGroup
		assert productGroup
		assert productGroup.products
		assert 1 == productGroup.products.size()
	}


    @Test
	void test_productGroup_hasManyProducts() {
		
		def product = Product.findByName("Ibuprofen, 200 mg, tablet")
		println product
		assert product
		
		def productGroup = ProductGroup.findByDescription("Ibuprofen")
		println productGroup
        assert productGroup


        assert "Ibuprofen, 200 mg, tablet" == product.name
        assert product.productGroups
        assert product.productGroups.contains(productGroup)

        assert "Ibuprofen" == productGroup.description
        assert productGroup.products
        assert productGroup.products.contains(product)
	}

    // This test doesn't fail any longer.
    @Ignore
	void delete_shouldFail() {
		def product = Product.findByName("Ibuprofen, 200 mg, tablet")
		assertNotNull product
		
		def productGroup = ProductGroup.findByDescription("Ibuprofen")
		assertNotNull productGroup

		def message = shouldFail(Exception) { 
			productGroup.delete(flush:true)
		}
		assertNotNull message
		assertTrue message.contains("deleted object would be re-saved by cascade") 
		
		product = Product.findByName("Ibuprofen, 200 mg, tablet")
		assertEquals 1, product.productGroups.size()
		assertEquals 1, productGroup.products.size()

	}

    @Test
	void test_delete_shouldDeleteProductGroupAndProductAssociation() {
		
		def product = Product.findByName("Ibuprofen, 200 mg, tablet")
        assert product
        println product.category
        println product.category.categories

		def productGroup = ProductGroup.findByDescription("Ibuprofen")
        assert productGroup

		product.removeFromProductGroups(productGroup)		
		productGroup.delete()
		
		product = Product.findByName("Ibuprofen, 200 mg, tablet")
        assert 0 == product.productGroups.size()
        assert 0 == productGroup.products.size()
	}

    @Test
	void test_productGroup_shouldNotPersistNonOwningAssociation() { 
		printAllProducts()
		def product = Product.findByName("Tylenol, 325 mg, tablet")
		assert product
				
		def productGroup = ProductGroup.findByDescription("Tylenol")
        assert productGroup
        assert productGroup.products
        assert 1 == productGroup.products.size()
        assert productGroup.products.contains(product)
	}
	
	void printAllProducts() { 
		println "PRINT ALL PRODUCTS"
		def products = Product.list()
		products.each {
			println it.id + " " +  it.name + " " +  it.category + " " +  it.productGroups
		}
	}
	
	List getProductGroupProducts() { 		
		println "PRINT ALL PRODUCT GROUP PRODUCTS"
		def orphans = []

		Sql sql = new Sql(dataSource)
		sql.eachRow("select product.id as product_id, product.name as product_name, product_group.id as product_group_id, product_group.description as product_group_name from product_group_product left outer join product_group on product_group_id = product_group.id join product on product_id = product.id;", { row -> 
			orphans << [row.product_id, row.product_name, row.product_group_id, row.product_group_name]
		})
		return orphans;
	}
	
}
