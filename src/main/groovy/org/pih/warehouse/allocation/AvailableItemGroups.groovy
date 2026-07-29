/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.allocation

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product

/**
 * Splits the available items for a product into the groups the ordering strategies combine:
 * display bins, preferred warehouse bins and the remaining warehouse bins. Computed once so each
 * handler only has to declare the order in which the groups are concatenated.
 */
class AvailableItemGroups {

    final List<AvailableItem> displayItems
    final List<AvailableItem> preferredWarehouseItems
    final List<AvailableItem> remainingWarehouseItems

    AvailableItemGroups(Location facility, Product product, List<AvailableItem> availableItems) {
        displayItems = availableItems?.findAll { it.binLocation?.isDisplay() } ?: []

        List<AvailableItem> warehouseItems = availableItems?.findAll { !it.binLocation?.isDisplay() } ?: []
        Location preferredBinLocation = InventoryLevel.findPreferredBinLocation(facility, product)
        preferredWarehouseItems = preferredBinLocation ? warehouseItems.findAll { it.binLocation == preferredBinLocation } : []
        remainingWarehouseItems = warehouseItems - preferredWarehouseItems
    }
}
