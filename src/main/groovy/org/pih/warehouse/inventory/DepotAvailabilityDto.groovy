package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.location.LocationSimpleDto

/**
 * How much of a product lot one depot holds.
 */
class DepotAvailabilityDto {

    LocationSimpleDto depot
    Integer quantityOnHand

    static DepotAvailabilityDto from(Location depot, Integer quantityOnHand) {
        return !depot ? null : new DepotAvailabilityDto(
                depot: LocationSimpleDto.from(depot),
                quantityOnHand: quantityOnHand,
        )
    }
}
