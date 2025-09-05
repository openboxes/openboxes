package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

class DefaultSlottingStrategy implements PutawayStrategy {

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {
        List<PutawayResult> putawayTasks = []
        if (context.preferredBin) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    location: context.currentBinLocation,
                    destination: context.preferredBin,
                    quantity: quantityRemaining,
            )
        }

        return putawayTasks
    }
}
