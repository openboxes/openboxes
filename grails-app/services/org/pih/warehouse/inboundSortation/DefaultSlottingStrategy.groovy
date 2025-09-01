package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location

class DefaultSlottingStrategy implements PutawayStrategy {

    @Override
    List<PutawayTask> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {
        List<PutawayTask> putawayTasks = []
        if (context.preferredBin) {
            putawayTasks << new PutawayTask(
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
