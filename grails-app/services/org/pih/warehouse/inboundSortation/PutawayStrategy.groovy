package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location

interface PutawayStrategy {
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining)
}