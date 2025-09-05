package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

interface PutawayStrategy {
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining)
}