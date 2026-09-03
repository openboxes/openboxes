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

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product

class CandidateLocationGroups {

    final List<Location> displayLocations
    final List<Location> preferredWarehouseLocations
    final List<Location> remainingWarehouseLocations

    CandidateLocationGroups(Location facility, Product product, List<Location> candidateLocations) {
        displayLocations = candidateLocations?.findAll { it.isDisplay() } ?: []

        List<Location> warehouseLocations = candidateLocations?.findAll { !it.isDisplay() } ?: []
        Location preferredBinLocation = InventoryLevel.findPreferredBinLocation(facility, product)
        preferredWarehouseLocations = preferredBinLocation ? warehouseLocations.findAll { it == preferredBinLocation } : []
        remainingWarehouseLocations = warehouseLocations - preferredWarehouseLocations
    }

    List<Location> locationsFor(AllocationSourceGroup group) {
        switch (group) {
            case AllocationSourceGroup.DISPLAY:
                return displayLocations
            case AllocationSourceGroup.PREFERRED_STORAGE:
                return preferredWarehouseLocations
            case AllocationSourceGroup.REMAINING_STORAGE:
                return remainingWarehouseLocations
            default:
                throw new IllegalArgumentException("Unsupported allocation source group: ${group}")
        }
    }
}
