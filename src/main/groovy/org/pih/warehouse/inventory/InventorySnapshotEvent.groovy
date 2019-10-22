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
import org.springframework.context.ApplicationEvent

class InventorySnapshotEvent extends ApplicationEvent {

    Product product
    Location binLocation
    InventoryItem inventoryItem

    InventorySnapshotEvent(InventoryItem source) {
        super(source)
        this.inventoryItem = source
    }

    InventorySnapshotEvent(Location source) {
        super(source)
        this.binLocation = source
    }

    InventorySnapshotEvent(Product source) {
        super(source)
        this.product = source
    }

}
