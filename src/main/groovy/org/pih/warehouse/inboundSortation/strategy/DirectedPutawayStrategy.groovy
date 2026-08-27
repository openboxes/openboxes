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

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

class DirectedPutawayStrategy implements PutawayStrategy {

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
                    container: resolvePutawayContainer(context, locations, context.preferredBin),
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
                    container: resolvePutawayContainer(context, locations, context.internalLocation),
                    quantity: quantityRemaining,
                    comment: "Default Internal Location",
            )
        }

        return putawayTasks
    }
}
