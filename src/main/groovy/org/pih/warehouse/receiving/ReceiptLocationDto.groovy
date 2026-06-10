package org.pih.warehouse.receiving

import org.pih.warehouse.core.Location

class ReceiptLocationDto {
    String id
    String name

    static ReceiptLocationDto toDto(Location location) {
        if (!location) {
            return null
        }
        return new ReceiptLocationDto(id: location.id, name: location.name)
    }
}
