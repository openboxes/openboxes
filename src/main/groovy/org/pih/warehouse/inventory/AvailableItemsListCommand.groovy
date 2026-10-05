package org.pih.warehouse.inventory

import org.pih.warehouse.api.PaginationCommand

/**
 * Request params for GET /api/facilities/:facilityId/availableItems
 */
class AvailableItemsListCommand extends PaginationCommand {

    String facilityId

    static constraints = {
        facilityId nullable: false, blank: false
    }
}
