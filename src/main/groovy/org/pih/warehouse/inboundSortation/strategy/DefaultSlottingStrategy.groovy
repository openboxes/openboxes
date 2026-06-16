package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

class DefaultSlottingStrategy implements PutawayStrategy {

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining, List<PutawayResult> putawayResults) {
        List<PutawayResult> putawayTasks = []
        if (context.preferredBin) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    location: context.currentBinLocation,
                    destination: context.preferredBin,
                    container: findPutawayContainerByZone(locations, context.preferredBin?.zone),
                    quantity: quantityRemaining,
                    comment: "Default Location",
            )
        } else if (context.internalLocation && context.internalLocation.supports(ActivityCode.UNDEFINED_LOCATION)) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    location: context.currentBinLocation,
                    destination: context.internalLocation,
                    container: findPutawayContainerByZone(locations, context.internalLocation?.zone),
                    quantity: quantityRemaining,
                    comment: "Default Internal Location",
            )
        }

        return putawayTasks
    }
}
