package org.pih.warehouse.allocation

import org.pih.warehouse.requisition.RequisitionItem

class AllocationApiController {
    AllocationService allocationService

    def allocate() {
        try {
            RequisitionItem requisitionItem = RequisitionItem.get(params.id)
            List<AllocationStrategy> strategyList = [AllocationStrategy.WAREHOUSE_FIRST]
             AllocationRequest allocationRequest = new AllocationRequest(requisitionItem: requisitionItem, allocationMode: AllocationMode.AUTO, allocationStrategies: strategyList)
             def result = allocationService.allocate(allocationRequest)
        } catch (Exception e) {
            response.status = 404
        }
    }
}
