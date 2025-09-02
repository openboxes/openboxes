package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location

class RandomSlottingStrategy implements PutawayStrategy {

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {
        List<PutawayResult> putawayTasks = []
        Location randomLocation = getRandomLocation(locations)
        if (randomLocation) {
            putawayTasks << new PutawayResult(
                    facility: context.facility,
                    product: context.product,
                    inventoryItem: context.inventoryItem,
                    currentBinLocation: context.currentBinLocation,
                    putawayLocation: randomLocation,
                    quantity: quantityRemaining,
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
