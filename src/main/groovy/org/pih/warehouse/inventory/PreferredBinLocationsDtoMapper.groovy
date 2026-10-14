package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.core.serialization.SerializationMapper
import org.pih.warehouse.location.LocationSimpleDto

@Component
class PreferredBinLocationsDtoMapper implements SerializationMapper<PreferredBinLocationsDto> {

    @Override
    Map<String, Object> serialize(PreferredBinLocationsDto source) {
        return source.preferredBinLocationsByProductId.collectEntries { String productId, LocationSimpleDto preferredBinLocation ->
            [(productId): preferredBinLocation]
        } as Map<String, Object>
    }
}
