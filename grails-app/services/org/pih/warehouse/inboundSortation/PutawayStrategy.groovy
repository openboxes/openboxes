package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location

interface PutawayStrategy {
    List<PutawayTask> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining)
}