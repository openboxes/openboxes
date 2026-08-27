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
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product

/**
 * Chooses the location that takes a stock movement when no location holds enough stock. It only makes
 * the choice and saves nothing, so callers stay responsible for whatever has to be recorded afterwards.
 */
@Transactional(readOnly = true)
class AllocationFallbackService {

    LocationService locationService
    ProductAvailabilityService productAvailabilityService

    AllocationSourceStrategyHandlerResolver allocationSourceStrategyHandlerResolver = new AllocationSourceStrategyHandlerResolver()

    /**
     * Chooses where the quantity nobody can supply is recorded, working through the allocation steps in
     * order and stopping at the first that succeeds. Firstly it looks for a bin permitted to hold a negative quantity,
     * secondly it falls back to the facility's shortfall location.
     */
    AllocationFallbackResolution resolve(Location facility, Product product, AllocationSourceStrategy strategy) {
        Location negativeInventoryLocation = resolveNegativeInventoryLocation(facility, product, strategy)
        if (negativeInventoryLocation) {
            return new AllocationFallbackResolution(negativeInventoryLocation, AllocationStep.NEGATIVE_INVENTORY)
        }

        Location shortfallLocation = locationService.getNegativeInventoryFallbackLocation(facility)
        if (!shortfallLocation) {
            log.warn("No inventory shortfall location configured for facility ${facility?.name}")
            return null
        }
        return new AllocationFallbackResolution(shortfallLocation, AllocationStep.FALLBACK_LOCATION)
    }

    private Location resolveNegativeInventoryLocation(Location facility, Product product, AllocationSourceStrategy strategy) {
        List<Location> candidateLocations = getCandidateBinLocations(facility, product)
                .findAll {
                    it.active && it.isAllocable() &&
                            !it.isNegativeInventoryFallbackLocation() && it.isNegativeInventoryAllowed()
                }

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

    /**
     * The bins that may take the shortfall for this product.
     */
    private List<Location> getCandidateBinLocations(Location facility, Product product) {
        List<Location> assignedLocations = InventoryLevel.getPutawayLocations(facility, product) ?: []
        List<Location> stockedLocations = productAvailabilityService
                .getAllAvailableBinLocations(facility, product?.id)
                ?.collect { it.binLocation }
                ?.findAll { it } ?: []

        return (assignedLocations + stockedLocations).unique { it.id }
    }
}
