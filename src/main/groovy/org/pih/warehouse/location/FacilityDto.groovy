package org.pih.warehouse.location

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.dtos.IdentifiableDto

/**
 * The DTO representation of a {@link Location} when the location is a facility.
 */
class FacilityDto implements IdentifiableDto {

    String name
    String description
    String locationNumber
    byte[] logo
    String address
    String fgColor
    String bgColor
    String parentLocation
    String locationType
    String locationGroup
    String organization
    String manager
    String inventory
    Boolean active
    Integer sortOrder
    Set<String> supportedActivities

    static FacilityDto from(Location location) {
        return !location ? null : new FacilityDto(
                id: location.id,
                name: location.name,
                description: location.description,
                locationNumber: location.locationNumber,
                logo: location.logo,
                address: location.address?.id,
                fgColor: location.fgColor,
                bgColor: location.bgColor,
                parentLocation: location.parentLocation?.id,
                locationType: location.locationType?.id,
                locationGroup: location.locationGroup?.id,
                organization: location.organization?.id,
                manager: location.manager?.id,
                inventory: location.inventory?.id,
                active: location.active,
                sortOrder: location.sortOrder,
                supportedActivities: location.supportedActivities,
        )
    }
}
