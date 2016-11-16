/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup

class InventoryItemSummary {

    String id
    Date date
    Product product
    ProductGroup productGroup
    InventoryItem inventoryItem
    Location location

    double quantity = 0
    double quantityAvailable = 0
    double quantityAllocated = 0
    double quantityInbound = 0
    double quantityOutbound = 0

    Date dateCreated
    Date lastUpdated

    static constraints = {
        product(nullable:false, unique: ['location', 'inventoryItem'])
        location(nullable:false)
        inventoryItem(nullable:false)
        quantity(nullable:false)
        quantityAvailable(nullable:true)
        quantityAllocated(nullable:true)
        quantityInbound(nullable:true)
        quantityOutbound(nullable:true)
    }

    static mapping = {
        cache true
        id generator: 'uuid'
    }

}
