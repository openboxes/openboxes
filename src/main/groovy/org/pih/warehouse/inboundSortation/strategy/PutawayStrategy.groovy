package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

trait PutawayStrategy {
    abstract List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining, List<PutawayResult> putawayResults)

    /**
     * Finds the putaway container associated with the given zone.
     */
    Location findPutawayContainerByZone(List<Location> locations, Location zone) {
        if (!zone) {
            return null
        }
        return locations.find { it.zone?.id == zone.id && it.supports(ActivityCode.PUTAWAY_CART) }
    }
}
