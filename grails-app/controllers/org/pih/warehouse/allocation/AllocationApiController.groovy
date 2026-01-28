package org.pih.warehouse.allocation

import org.pih.warehouse.requisition.Requisition

class AllocationApiController {
    AllocationService allocationService

    def allocate() {
        try {
            Requisition requisition = Requisition.get(params.id)
            List<AllocationStrategy> strategyList = grailsApplication.config.openboxes.order.allocation.strategies
            allocationService.allocate(requisition, AllocationMode.AUTO, strategyList)
        } catch (Exception e) {
            response.status = 404
        }
    }
}
