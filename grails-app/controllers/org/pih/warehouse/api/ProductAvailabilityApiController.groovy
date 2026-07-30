package org.pih.warehouse.api

import grails.converters.JSON
import org.hibernate.ObjectNotFoundException

import org.pih.warehouse.core.Location
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
    def list() {
        Location location = Location.get(params.facilityId)
        if (!location) {
            throw new ObjectNotFoundException(params.facilityId, Location.class.toString())
        }

        Integer max = Math.min(params.max ? params.int("max") : 10, 100)
        Integer offset = params.offset != null ? params.int("offset") : 0

        List availableItems = productAvailabilityService.getAvailableItems(
                location, null, false, true, [max: max, offset: offset])
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
