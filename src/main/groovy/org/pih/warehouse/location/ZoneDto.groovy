package org.pih.warehouse.location

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.dtos.IdentifiableDto

/**
 * The DTO representation of a {@link Location} when the location is a zone.
 */
class ZoneDto implements IdentifiableDto {

    String name
    String description
    String locationNumber
    Boolean active
    String parentLocationId
    String locationTypeId
    String locationGroupId
    String organizationId

    static ZoneDto from(Location location) {
        return !location ? null : new ZoneDto(
                id: location.id,
                name: location.name,
                description: location.description,
                locationNumber: location.locationNumber,
                active: location.active,
                parentLocationId: location.parentLocation?.id,
                locationTypeId: location.locationType?.id,
                locationGroupId: location.locationGroup?.id,
                organizationId: location.organization?.id,
        )
    }
}
