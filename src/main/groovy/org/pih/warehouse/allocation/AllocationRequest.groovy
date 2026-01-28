package org.pih.warehouse.allocation

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.requisition.RequisitionItem

class AllocationRequest {
    Integer quantityRequired
    RequisitionItem requisitionItem
    AllocationMode allocationMode
    List<AvailableItem> availableItems
    List<AllocationStrategy> allocationStrategies

    static constraints = {
        quantityRequired(nullable: false)
        requisitionItem(nullable: false)
        allocationMode(nullable: false)
        availableItems(nullable: true)
        allocationStrategies(nullable: true)
    }
}
