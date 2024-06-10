package org.pih.warehouse.api

import org.pih.warehouse.picklist.PicklistService

class PicklistApiController extends BaseDomainApiController {

    PicklistService picklistService

    def clearPicklist() {
        picklistService.clearPicklist(params.id)

        render status: 204
    }
}
