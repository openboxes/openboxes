package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

class RandomSlottingStrategy implements PutawayStrategy {

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining, List<PutawayResult> putawayResults) {
        List<PutawayResult> putawayTasks = []
        Location destination = getRandomLocation(locations)
        if (destination) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    location: context.currentBinLocation,
                    destination: destination,
                    container: resolvePutawayContainer(context, locations, destination),
                    quantity: quantityRemaining,
                    comment: "Random",
            )
        }

        return putawayTasks
    }

    private Location getRandomLocation(List<Location> locations) {
        if (locations.isEmpty()) {
            return null
        }

        Random random = new Random()
        int randomIndex = random.nextInt(locations.size())

        return locations[randomIndex]
    }
}
