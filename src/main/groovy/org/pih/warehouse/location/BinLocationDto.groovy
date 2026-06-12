package org.pih.warehouse.location

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * Represents a Location simplified down to only the fields relevant for bin locations.
 */
class BinLocationDto implements ResponseBodyFormattable {

    String id
    String name
    String locationNumber
    Boolean active

    static BinLocationDto from(Location location) {
        if (!location) {
            return null
        }
//        if (!location.isBinLocation()) {
//            throw new IllegalArgumentException("Only a bin location can be converted to a BinLocationDto.")
//        }

        return new BinLocationDto(
                id: location.id,
                name: location.name,
                locationNumber: location.locationNumber,
                active: location.active,
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                id: id,
                name: name,
                locationNumber: locationNumber,
                active: active,
        ]
    }
}
