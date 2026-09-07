package org.pih.warehouse.inboundSortation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.inboundSortation.strategy.PutawayStrategy

// FIXME: This should be called InboundRoutingStrategyServiceand all of the other classes should follow suit
//  (PutawayStrategy -> RoutingStrategy, PutawayResult -> RoutingResult).
//  see: https://github.com/openboxes/openboxes/pull/6030/changes#r3609848674
@Transactional
class PutawayStrategyService {
    List<PutawayStrategy> strategies

    List<PutawayResult> execute(PutawayContext context) {
        List<PutawayResult> results = []
        int quantityRemaining = context.quantity
        def locations = context.facility.activeStorageLocations

        for (PutawayStrategy strategy in strategies) {
            if (quantityRemaining <= 0) break
            def tasks = strategy.execute(context, locations, quantityRemaining, results)

            if (tasks) {
                results.addAll(tasks)
                quantityRemaining -= tasks*.quantity.sum(0) as int
            }
        }

        if (quantityRemaining > 0) {
            log.warn("No putaway destination for ${quantityRemaining} of product " +
                    "${context.product?.productCode} in facility ${context.facility?.name}")
        }

        return results
    }
}
