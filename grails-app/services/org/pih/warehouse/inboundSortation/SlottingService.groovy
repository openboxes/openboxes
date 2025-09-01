package org.pih.warehouse.inboundSortation

class SlottingService {
    List<PutawayStrategy> strategies

    List<PutawayTask> execute(PutawayContext context) {
        List<PutawayTask> putawayTasks = []
        int quantityRemaining = context.quantity
        def locations = context.facility.internalLocations

        for (PutawayStrategy strategy in strategies) {
            if (quantityRemaining <= 0) break
            def tasks = strategy.execute(context, locations, quantityRemaining)

            if (tasks) {
                putawayTasks.addAll(tasks)
                quantityRemaining -= tasks*.quantity.sum(0) as int
            }
        }

        if (quantityRemaining > 0) {
            throw RuntimeException("Quantity remaining should not be 0 at this point. Something went wrong.")
        }

        return putawayTasks
    }
}
