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

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product

/**
 * Chooses the location that takes a stock movement when no location holds enough stock. It only makes
 * the choice and saves nothing, so callers stay responsible for whatever has to be recorded afterwards.
 */
@Transactional(readOnly = true)
class AllocationFallbackService {

    LocationService locationService

    AllocationSourceStrategyHandlerResolver allocationSourceStrategyHandlerResolver = new AllocationSourceStrategyHandlerResolver()

    AllocationFallbackResolution resolve(Location facility, Product product, AllocationSourceStrategy strategy) {
        Location negativeInventoryLocation = resolveNegativeInventoryLocation(facility, product, strategy)
        if (negativeInventoryLocation) {
            return new AllocationFallbackResolution(negativeInventoryLocation, AllocationStep.NEGATIVE_INVENTORY)
        }

        Location shortfallLocation = locationService.getInventoryShortfallLocation(facility)
        if (!shortfallLocation) {
            throw new IllegalStateException(
                    "No inventory shortfall location configured for facility ${facility?.name}")
        }
        return new AllocationFallbackResolution(shortfallLocation, AllocationStep.INVENTORY_SHORTFALL)
    }

    private Location resolveNegativeInventoryLocation(Location facility, Product product, AllocationSourceStrategy strategy) {
        List<Location> candidateLocations = getCandidateBinLocations(facility, product)
                .findAll { !it.isInventoryShortfallLocation() && it.isNegativeInventoryAllowed() }

        if (!candidateLocations) {
            return null
        }

        AllocationSourceStrategyHandler handler = allocationSourceStrategyHandlerResolver.handlerFor(strategy)
        if (!handler) {
            log.warn("No allocation source strategy handler registered for ${strategy}, using natural order")
            return candidateLocations.first()
        }

        return handler.orderLocations(facility, product, candidateLocations)?.find()
    }

    private static List<Location> getCandidateBinLocations(Location facility, Product product) {
        return InventoryLevel.getPutawayLocations(facility, product)?.unique() ?: []
    }
}
