package org.pih.warehouse.api

import grails.converters.JSON
import org.hibernate.ObjectNotFoundException

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.AvailableItemsListCommand
import org.pih.warehouse.inventory.ProductAvailabilityService

/**
 * Facility-scoped product availability APIs.
 */
class ProductAvailabilityApiController {

    ProductAvailabilityService productAvailabilityService

    /**
     * Available items for a facility (paginated). Excludes zero quantity-on-hand rows.
     * No unbounded full-dump endpoint — large facilities make that a worst-case footgun;
     * clients that need everything can page through this API.
     */
    def list(AvailableItemsListCommand command) {
        Location location = Location.get(command.facilityId)
        if (!location) {
            throw new ObjectNotFoundException(command.facilityId, Location.class.toString())
        }

        List availableItems = productAvailabilityService.getAvailableItems(
                location, null, false, true, command.paginationParams)
        render([data: toAvailableItemsJson(location, availableItems), totalCount: availableItems.totalCount] as JSON)
    }

    private List toAvailableItemsJson(Location location, List availableItems) {
        return availableItems.collect { AvailableItem availableItem ->
            Map json = availableItem.toJson()
            json.location = [
                    id  : location.id,
                    name: location.name
            ]
            return json
        }
    }
}
