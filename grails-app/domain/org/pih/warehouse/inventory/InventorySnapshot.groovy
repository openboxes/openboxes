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


class InventorySnapshot implements java.io.Serializable {

    String id

    // Core data elements
    Date date
    Location location
    Location binLocation
    Product product
    InventoryItem inventoryItem

    String productCode
    String lotNumber
    String binLocationName

    Integer quantityOnHand
    Integer quantityInbound
    Integer quantityOutbound

    // Auditing
    Date dateCreated
    Date lastUpdated


    static mapping = {
        id generator: 'uuid'
    }

    // Constraints
    static constraints = {
        date(nullable: true)
        product(nullable: false)
        location(nullable: false)
        binLocation(nullable: true)
        inventoryItem(nullable: true)
        quantityOnHand(nullable: true)
        quantityInbound(nullable: true)
        quantityOutbound(nullable: true)
    }

    static namedQueries = {
        firstUpdatedDate { locationId ->
            projections {
                min 'lastUpdated'
            }
            eq("location.id", locationId)
            uniqueResult = true
        }

        lastUpdatedDate { locationId ->
            projections {
                max 'lastUpdated'
            }
            eq("location.id", locationId)
            uniqueResult = true
        }
    }

}
