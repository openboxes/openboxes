package org.pih.warehouse.api

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem

class InventoryItemApiController {

    def locationService

    def listByActivity() {
        Location facility = Location.get(params.facilityId)

        ActivityCode activityCode
        try {
            activityCode = ActivityCode.valueOf(params.activityCode ?: "")
        } catch (ignored) {
            render(status: 400, text: [error: "Invalid activityCode"])
            return
        }

        def locations = locationService.getLocationsSupportingActivity(activityCode)
        def locationIds = locations.findAll { it.parentLocation?.id == facility.id }*.id

        if (!locationIds) {
            render([])
            return
        }

        // Fetch all inventory items for those bin locations
        def items = InventoryItem.executeQuery("""
            SELECT DISTINCT pa.inventoryItem
            FROM ProductAvailability pa
            WHERE pa.location = :facility
              AND pa.binLocation.id IN (:locationIds)
        """, [facility: facility, locationIds: locationIds])

        render(items.collect { it.toJson() })
    }
}
