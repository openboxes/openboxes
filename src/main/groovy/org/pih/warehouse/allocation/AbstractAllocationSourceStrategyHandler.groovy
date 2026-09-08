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
import org.pih.warehouse.product.Product

/**
 * Splits the available items into groups once and lets each concrete handler declare
 * only the order in which those groups are concatenated.
 */
abstract class AbstractAllocationSourceStrategyHandler implements AllocationSourceStrategyHandler {

    @Override
    List<AvailableItem> orderAvailableItems(Location facility, Product product, List<AvailableItem> availableItems) {
        AvailableItemGroups groups = new AvailableItemGroups(facility, product, availableItems)
        return groupOrder.collectMany { groups.itemsFor(it) }
    }

    @Override
    List<Location> orderLocations(Location facility, Product product, List<Location> candidateLocations) {
        CandidateLocationGroups groups = new CandidateLocationGroups(facility, product, candidateLocations)
        return groupOrder.collectMany { groups.locationsFor(it) }
    }
}
