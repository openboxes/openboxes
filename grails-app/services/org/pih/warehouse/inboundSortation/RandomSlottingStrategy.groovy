package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.Location

class RandomSlottingStrategy implements PutawayStrategy {

    @Override
    List<PutawayTask> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {
        List<PutawayTask> putawayTasks = []
        Location randomLocation = getRandomLocation(locations)
        if (randomLocation) {
            putawayTasks << new PutawayTask(
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
