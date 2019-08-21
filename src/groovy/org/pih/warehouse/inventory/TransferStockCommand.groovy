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

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order

@Validateable
class TransferStockCommand {

    Integer quantity = 0
    InventoryItem inventoryItem
    Location location
    Location binLocation

    // Determines whether this is a transfer in or out (and whether we populate source or destination)
    Boolean transferOut
    Location otherLocation
    Location otherBinLocation

    Order order

    static constraints = {
        quantity(nullable: false)
        inventoryItem(nullable: false)
        location(nullable: false)
        binLocation(nullable: false)
        transferOut(nullable: false)
        otherLocation(nullable: false)
        otherBinLocation(nullable: false)
        order(nullable: true)
    }
}


