package org.pih.warehouse.location

import org.pih.warehouse.core.Location

/**
 * A simplified representation of a Location, containing only the fields that are required
 * to display the location in its most basic form.
 */
class LocationSimpleDto {

    String id
    String name
    String locationNumber

    static LocationSimpleDto from(Location location) {
        return !location ? null : new LocationSimpleDto(
                id: location.id,
                name: location.name,
                locationNumber: location.locationNumber,
        )
    }
}
