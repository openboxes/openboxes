package org.pih.warehouse.api

import javax.validation.Valid

import org.pih.warehouse.inventory.ListAvailableItemsCommand
import org.pih.warehouse.inventory.ProductAvailabilityService

/**
 * Facility-scoped product availability APIs.
 */
class ProductAvailabilityApiController extends BaseApiController {

    ProductAvailabilityService productAvailabilityService

    /**
     * Fetch a paginated list of all available items for a facility. Excludes zero quantity-on-hand rows.
     */
    def list(@Valid ListAvailableItemsCommand command) {
        List availableItems = productAvailabilityService.getAvailableItems(
                command.facility, null, false, true, command.paginationParams)
        renderResponse(availableItems)
    }
}
