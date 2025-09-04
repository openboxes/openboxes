package org.pih.warehouse.inboundSortation

import grails.gorm.transactions.Transactional

@Transactional
class SlottingService {
    List<PutawayStrategy> strategies

    List<PutawayResult> execute(PutawayContext context) {
        List<PutawayResult> putawayTasks = []
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

        // FIXME There should not be any quantity remaining at this point unless
        //  we've create a strategy that takes location capacity into account
        //  (i.e. strategies that limit the quantity allowed per strategy)
        if (quantityRemaining > 0) {
            throw RuntimeException("Quantity remaining should be 0 at this point. Something went wrong.")
        }

        return putawayTasks
    }
}
