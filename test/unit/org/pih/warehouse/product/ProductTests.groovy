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

class ProductTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }
	
    void testToJson() {
        Product product = new Product(id: "prod1", name: "product1")
        InventoryItem item1 = new InventoryItem(id: "item1", product:product)
        InventoryItem item2 = new InventoryItem(id: "item2", product:product)
        InventoryItem item3 = new InventoryItem(id: "item3", product:product)
        InventoryItem item4 = new InventoryItem(id: "item4", product:product)
        product.inventoryItems = [item1, item2, item3, item4]
        mockDomain(Product, [product])
        mockDomain(InventoryItem, [item1, item2, item3, item4])

        def map = product.toJson()

        assert map.id == product.id
        assert map.name == product.name
//        assert map.inventoryItems.any { it.inventoryItemId == item1.id }
//        assert map.inventoryItems.any { it.inventoryItemId == item2.id }
//        assert map.inventoryItems.any { it.inventoryItemId == item3.id }
//        assert map.inventoryItems.any { it.inventoryItemId == item4.id }

    }
}
