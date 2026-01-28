package org.pih.warehouse.allocation

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.requisition.RequisitionItem

class AllocationRequest {
    Integer quantityRequired
    RequisitionItem requisitionItem
    AllocationMode allocationMode
    List<AvailableItem> availableItems
    List<AllocationStrategy> allocationStrategies
}
