package org.pih.warehouse.inventory

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location

class InventoryItemService {

    def locationService

    Map listByActivity(Location facility, ActivityCode activityCode) {

        def locations = locationService.getLocationsSupportingActivity(activityCode)
        def locationIds = locations.findAll { it.parentLocation?.id == facility.id }*.id

        if (!locationIds) {
            return [items: [], totalCount: 0]
        }

        def items = InventoryItem.executeQuery("""
            SELECT DISTINCT pa.inventoryItem
            FROM ProductAvailability pa
            WHERE pa.location = :facility
              AND pa.binLocation.id IN (:locationIds)
        """, [facility: facility, locationIds: locationIds])

        return [items: items, totalCount: items.size()]
    }

    Long countByActivity(Location facility, ActivityCode activityCode) {
        def locations = locationService.getLocationsSupportingActivity(activityCode)
        def locationIds = locations.findAll { it.parentLocation?.id == facility.id }*.id

        if (!locationIds) {
            return 0L
        }

        def results = InventoryItem.executeQuery("""
            SELECT COUNT(DISTINCT pa.inventoryItem.id)
            FROM ProductAvailability pa
            WHERE pa.location = :facility
              AND pa.binLocation.id IN (:locationIds)
        """, [facility: facility, locationIds: locationIds])

        return results[0] as Long ?: 0L
    }
}
