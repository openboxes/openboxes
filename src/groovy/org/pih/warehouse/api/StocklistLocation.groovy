package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition

class StocklistLocation {

    Location location
    List<StocklistItem> stocklistItems

    List<Requisition> availableStocklists

    Map toJson() {
        return [
                "location.id": location?.id,
                "location.name": location?.name,
                "locationGroup.id": location?.locationGroup?.id,
                "locationGroup.name": location?.locationGroup?.name,
                stocklistItems: stocklistItems,
                monthlyDemand: stocklistItems?.collect { it.requisitionItem?.quantity ?: 0 }?.sum(),
                availableStocklists: availableStocklists.collect { [ id: it.id, name: it.name ] },
        ]
    }
}
