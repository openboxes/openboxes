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

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem

class ProductAvailability {

    String id

    // Foreign keys
    Product product
    Location location
    Location binLocation
    InventoryItem inventoryItem

    // Unique constraint
    String productCode
    String lotNumber
    String binLocationName

    // Quantities
    Integer quantityOnHand
    Integer quantityAllocated
    Integer quantityOnHold
    Integer quantityAvailableToPromise

    // Auditing
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: "assigned"
    }

    static constraints = {
        product(nullable:false)
        location(nullable:false)
        binLocation(nullable:true)
        inventoryItem(nullable:false)
        quantityOnHand(nullable:false)
        quantityAllocated(nullable: true)
        quantityOnHold(nullable: true)
        quantityAvailableToPromise(nullable: true)
    }
}
