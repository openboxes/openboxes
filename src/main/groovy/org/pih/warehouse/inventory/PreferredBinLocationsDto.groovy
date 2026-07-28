package org.pih.warehouse.inventory

import org.pih.warehouse.core.http.ResponseBodyFormattable
import org.pih.warehouse.location.LocationSimpleDto

/**
 * The preferred bin locations of a set of products at a given facility, keyed by product id.
 * Products with no preferred bin location configured are omitted.
 */
class PreferredBinLocationsDto implements ResponseBodyFormattable {

    Map<String, LocationSimpleDto> preferredBinLocationsByProductId = [:]

    static PreferredBinLocationsDto from(List<InventoryLevel> inventoryLevels) {
        Map<String, LocationSimpleDto> preferredBinLocationsByProductId =
                inventoryLevels.collectEntries { InventoryLevel inventoryLevel ->
                    [(inventoryLevel.product.id): LocationSimpleDto.from(inventoryLevel.preferredBinLocation)]
                }
        return new PreferredBinLocationsDto(preferredBinLocationsByProductId: preferredBinLocationsByProductId)
    }

    @Override
    Map<String, Object> asResponseBody() {
        return preferredBinLocationsByProductId.collectEntries { String productId, LocationSimpleDto preferredBinLocation ->
            [(productId): preferredBinLocation]
        } as Map<String, Object>
    }
}
