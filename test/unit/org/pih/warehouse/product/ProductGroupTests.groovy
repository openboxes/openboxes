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
import org.pih.warehouse.core.User
import org.springframework.context.ApplicationEvent

// import org.pih.warehouse.core.Document;
// import org.pih.warehouse.inventory.InventoryItem

class ProductGroupTests extends GrailsUnitTestCase {
	protected void setUp() {
		super.setUp()

        Product.metaClass.static.withNewSession = {Closure c -> c.call() }
        User.metaClass.static.withNewSession = {Closure c -> c.call() }
        Product.metaClass.publishEvent = { ApplicationEvent event -> }
    }

	protected void tearDown() {
		super.tearDown()
	}
	
	void testProductGroup() {
		def productGroup = new ProductGroup()
		def category = new Category()
		assertNotNull(productGroup)
		mockDomain(ProductGroup, [productGroup])
		//assertEquals(productGroup.name,'Ibuprofen, 200 mg, tablet')
		//assertEquals(productGroup.description,'NSAID ')
		//assertEquals(productGroup.category, category)

		//mockForConstraintsTests(ProductGroup, [productGroup])

		assertFalse productGroup.validate()
		assertEquals 1, productGroup.errors.errorCount
		assertEquals "nullable", productGroup.errors["name"]

		productGroup = new ProductGroup(name: "Name", description: "Description", category: category)
	}
	
	void testProductGroupHasManyProducts() { 
		def category = new Category(id:"1", name: "Medicines")
		def product = new Product(id:"1", name: "Ibuprofen", category: category)
		def productGroup = new ProductGroup(id:"1", name:"Ibuprofen", category: category)
		mockDomain(Category, [category])
		mockDomain(ProductGroup, [productGroup])
		mockDomain(Product, [product])		
		category.save(failOnError:true,flush:true)
		product.save(failOnError:true,flush:true)
		//productGroup.save(failOnError:true,flush:true)
		println productGroup
		
		println product
		println category
		println product.id
		println category.id
		assertNotNull product.id
		assertNotNull category.id
		
		product.addToProductGroups(productGroup)
		product.save(flush:true, failOnError:true)
		
		assertNotNull product.productGroups
		assertEquals 1, product.productGroups.size()
		
		// Apparently the bi-directional association does not work in unit tests
		productGroup = ProductGroup.get(productGroup.id)
		println productGroup
		println productGroup.products
		//assertNotNull productGroup.products
		//assertEquals 1, productGroup.products.size()
	}
}
