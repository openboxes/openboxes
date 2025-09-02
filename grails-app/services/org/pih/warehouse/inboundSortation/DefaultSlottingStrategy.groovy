package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location

class DefaultSlottingStrategy implements PutawayStrategy {

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {
        List<PutawayResult> putawayTasks = []
        if (context.preferredBin) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    currentBinLocation: context.currentBinLocation,
                    putawayLocation: context.preferredBin,
                    quantity: quantityRemaining,
            )
        }

        return putawayTasks
    }
}
