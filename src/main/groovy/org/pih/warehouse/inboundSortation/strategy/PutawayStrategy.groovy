package org.pih.warehouse.inboundSortation.strategy

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult

trait PutawayStrategy {
    abstract List<PutawayResult> execute(PutawayContext context, List<Location> locations, Integer quantityRemaining, List<PutawayResult> putawayResults)

    /**
     * Assigns a putaway container (cart) for the given destination based on the container
     * assignment strategy configured on the facility via activity codes. Returns null when no
     * assignment strategy is enabled or no matching container is found.
     */
    Location resolvePutawayContainer(PutawayContext context, List<Location> locations, Location destination) {
        Location facility = context.facility
        if (facility?.supports(ActivityCode.PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE)) {
            return resolvePutawayContainerByDeliveryType(locations, context.deliveryTypeCode)
        }
        if (facility?.supports(ActivityCode.PUTAWAY_CONTAINER_ASSIGNMENT_BY_ZONE)) {
            return resolvePutawayContainerByZone(locations, destination?.zone)
        }
        return null
    }

    /**
     * Resolves the putaway container associated with the given zone.
     */
    private Location resolvePutawayContainerByZone(List<Location> locations, Location zone) {
        if (!zone) {
            return null
        }
        return locations.find { it.zone?.id == zone.id && it.supports(ActivityCode.PUTAWAY_CART) }
    }

    /**
     * Resolves the putaway container associated with the given delivery type code.
     */
    private Location resolvePutawayContainerByDeliveryType(List<Location> locations, DeliveryTypeCode deliveryTypeCode) {
        ActivityCode deliveryActivityCode = deliveryTypeCode?.activityCode
        if (!deliveryActivityCode) {
            return null
        }
        return locations.find { it.supports(deliveryActivityCode) && it.supports(ActivityCode.PUTAWAY_CART) }
    }
}
