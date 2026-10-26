/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inboundSortation.strategy

import groovy.util.logging.Slf4j
import org.pih.warehouse.allocation.BackorderMatch
import org.pih.warehouse.allocation.BackorderMatchingService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

@Slf4j
class CrossDockPutawayStrategy implements PutawayStrategy {

    BackorderMatchingService backorderMatchingService

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining, List<PutawayResult> putawayResults) {

        List<PutawayResult> putawayTasks = []
        if (!context.backorderReference && !context.backorderItem) {
            return putawayTasks
        }

        BackorderMatch match = backorderMatchingService.resolveCrossDockMatch(
                context.backorderReference, context.backorderItem, context.product, quantityRemaining)
        if (!match) {
            log.warn("No demand left to cover for product ${context.product?.productCode} on backorder " +
                    "${context.backorderReference ?: context.backorderItem?.requisition?.requestNumber}. " +
                    "Quantity will be putaway to storage.")
            return putawayTasks
        }

        // delivery type is only known once the backorder/requisition has been resolved, so populate it on the context
        // before delegating the container assignment to the facility-configured strategy
        context.deliveryTypeCode = match.demand.requisition.deliveryTypeCode
        ActivityCode deliveryActivityCode = context.deliveryTypeCode?.activityCode
        Location destination = locations.find {
            deliveryActivityCode && it.supports(ActivityCode.CROSS_DOCKING) && it.supports(deliveryActivityCode)
        }
        if (!destination) {
            log.warn("No cross-dock zone found for delivery type ${context.deliveryTypeCode} in facility " +
                    "${context.facility?.name}. Quantity will be putaway to storage.")
            return putawayTasks
        }

        putawayTasks << new PutawayResult(
                facility: context.facility,
                product: context.product,
                inventoryItem: context.inventoryItem,
                location: context.currentBinLocation,
                destination: destination,
                container: resolvePutawayContainer(context, locations, destination),
                quantity: Math.min(match.quantityMatched, quantityRemaining),
                comment: "Cross-Docking",
        )
        return putawayTasks
    }
}
