package org.pih.warehouse.api

import org.pih.warehouse.picklist.PicklistService

class PicklistApiController extends BaseDomainApiController {

    PicklistService picklistService

    def clearPicklist() {
        picklistService.clearPicklist(params.id)

        render status: 204
    }

    def revertPick() {
        picklistService.revertPick(params.id, params.itemId)

        render status: 204
    }
}
