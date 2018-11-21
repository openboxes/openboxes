package org.pih.warehouse.api

import org.pih.warehouse.core.Location

class StocklistLocation {

    Location location
    List<Stocklist> stocklists

    Map toJson() {
        return [
                "location.id": location?.id,
                "location.name": location?.name,
                "locationGroup.id": location?.locationGroup?.id,
                "locationGroup.name": location?.locationGroup?.name,
                stocklists: stocklists
        ]
    }
}
