package org.pih.warehouse.location

import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.dtos.IdentifiableDto

/**
 * The DTO representation of a {@link org.pih.warehouse.core.LocationType}.
 */
class LocationTypeDto implements IdentifiableDto {

    String name
    String description
    Integer sortOrder
    Date dateCreated
    Date lastUpdated
    LocationTypeCode locationTypeCode
    Set<String> supportedActivities

    static LocationTypeDto from(LocationType locationType) {
        return !locationType ? null : new LocationTypeDto(
                id: locationType.id,
                name: locationType.name,
                description: locationType.description,
                sortOrder: locationType.sortOrder,
                dateCreated: locationType.dateCreated,
                lastUpdated: locationType.lastUpdated,
                locationTypeCode: locationType.locationTypeCode,
                supportedActivities: locationType.supportedActivities,
        )
    }
}
