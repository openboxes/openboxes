package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

class CrossDockingStrategy implements PutawayStrategy {

    def demandService

    @Override
    List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining) {

        List<PutawayResult> putawayTasks = []


        int availableQuantity = quantityRemaining
        if (availableQuantity <= 0) {
            return putawayTasks
        }

        // FIXME When we calculate unmet demand, we return a map indexed by delivery type code
        //  PICKUP: 5, LOCAL_DELIVERY: 25, SERVICE:10, WILL_CALL: 4, SHIP_TO: 0, DEFAULT: 200
        //
        // TODO The crucial part of getting this to work will be to make sure the putaway tasks are persisted immediately
        //  after each execution of the strategies so the calculation takes these into account.
        Map<DeliveryTypeCode, Integer> unmetDemandsByDeliveryType =
                demandService.calculateUnmetDemand(context.facility, context.product)

        // Walk through each of the delivery type codes to check if there's unmet demand. The DEFAULT delivery type
        // code just means that there is none, so it's ok ignore this demand type (it'll be ignored because there
        // probably won't be a destination associated with it) and for the stock to be putaway to storage and picked
        // from there. All delivery type codes that aren't associated with destinations through the activity code
        // will likely end up being putaway to storage.
        for (DeliveryTypeCode deliveryTypeCode in unmetDemandsByDeliveryType.keySet()) {
            // Find the first location that supports the activity code associated with the delivery type code
            //  i.e. a location that supports ActivityCode.DELIVERY_TYPE_SERVICE will be returned for demand
            //  lines that are DeliveryTypeCode.SERVICE.
            // FIXME this should find the first available location (i.e. quantity on hand = 0)
            //  or we need to figure out which order is associated the location so we put items
            //  from the same order in the same place.
            // FIXME We also probably need to find a solution when delivery type code is DeliveryTypeCode.DEFAULT
            Location destination = locations.find { Location location ->
                def supportsActivity = deliveryTypeCode.activityCode && location.supports(deliveryTypeCode.activityCode)
                def isNotPutawayContainer = location.locationType?.name != Constants.PUTAWAY_CONTAINER_TYPE

                return supportsActivity && isNotPutawayContainer
            }

            // FIXME If there's no suitable destination for the demand type, but there is quantity demanded we might
            //  want to signal or log something to ensure
            if (destination) {
                Location putawayContainer = locations.find { Location candidate ->
                    boolean supportsActivity = candidate.supports(deliveryTypeCode.activityCode)
                    boolean isPutawayContainerType = candidate.locationType?.name == Constants.PUTAWAY_CONTAINER_TYPE

                    return supportsActivity && isPutawayContainerType
                }

                Integer quantityDemanded = unmetDemandsByDeliveryType.get(deliveryTypeCode)
                int quantityForDemand = Math.min(quantityDemanded, availableQuantity)
                putawayTasks << new PutawayResult(
                        facility: context.facility,
                        product: context.product,
                        inventoryItem: context.inventoryItem,
                        location: context.currentBinLocation,
                        destination: destination,
                        quantity: quantityForDemand,
                        container: putawayContainer
                )

                availableQuantity -= quantityForDemand

                if (availableQuantity <= 0) {
                    break
                }
            }
        }

        return putawayTasks
    }
}
