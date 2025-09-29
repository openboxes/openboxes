package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItemService

class InventoryItemApiController {

    InventoryItemService inventoryItemService

    def listByActivity() {
        Location facility = Location.get(params.facilityId)

        ActivityCode activityCode
        try {
            activityCode = ActivityCode.valueOf(params.activityCode ?: "")
        } catch (ignored) {
            render(status: 400, text: [error: "Invalid activityCode"])
            return
        }

        def result = inventoryItemService.listByActivity(facility, activityCode)

        render([
                data      : result.items.collect { it.toJson() },
                totalCount: result.totalCount
        ] as JSON)
    }
}