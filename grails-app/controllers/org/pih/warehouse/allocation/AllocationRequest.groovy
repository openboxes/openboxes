package org.pih.warehouse.allocation

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.requisition.RequisitionItem

class AllocationRequest {

    RequisitionItem requisitionItem
    List<AvailableItem> availableItems
    List<AvailableItem> suggestedItems
    Integer quantityRequired

    static constraints = {
        requisitionItem nullable:true
        availableItems nullable:false
        suggestedItems nullable:true
        quantityRequired nullable:false
    }

}
