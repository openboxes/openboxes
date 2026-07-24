package org.pih.warehouse.location

import org.pih.warehouse.core.Location

/**
 * The DTO representation of a {@link Location} when the location is a bin location.
 *
 * Note that while they should likely be considered their own type, we often serialize internal locations
 * (such as temporary receiving bins) as bin locations.
 */
class BinLocationDto {

    String id
    String name
    String description
    String locationNumber
    Boolean active
    String parentLocation
    String locationType
    String locationGroup
    String organization
    String zone
    Set<String> supportedActivities

    static BinLocationDto from(Location location) {
        return !location ? null : new BinLocationDto(
                id: location.id,
                name: location.name,
                description: location.description,
                locationNumber: location.locationNumber,
                active: location.active,
                parentLocation: location.parentLocation?.id,
                locationType: location.locationType?.id,
                locationGroup: location.locationGroup?.id,
                organization: location.organization?.id,
                zone: location.zone?.id,
                supportedActivities: location.supportedActivities,
        )
    }
}
