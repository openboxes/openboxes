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
import org.pih.warehouse.inventory.InventoryItem

class ProductGroupIntegrationTests extends GroovyTestCase {
	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	
	void testProductGroupHasManyProducts() { 
		def category = new Category(id: "1", name: "Medicines")
		category.save(flush:true)
		def product = new Product(id: "1", name: "Ibuprofen", category: category)
		product.save(failOnError:true)
		
		def productGroup = new ProductGroup(id: "1", description:"Ibuprofen", category: category)		
		productGroup.save(flush:true)
		
		product.addToProductGroups(productGroup)
		
		assertNotNull product.id
		println product.id
		
		assertNotNull productGroup.products
		assertEquals 1, productGroup.products.size()		
	}
}
