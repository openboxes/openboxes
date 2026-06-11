package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Product

class CycleCountCandidate {

    String id

    Inventory inventory

    Product product

    Location facility

    CycleCountRequest cycleCountRequest

    String abcClass

    CycleCountCandidateStatus status

    String internalLocations

    Integer quantityOnHand

    Integer quantityAllocated

    Integer inventoryItemCount

    Integer negativeItemCount

    Date dateLastCount

    Date dateNextCount

    Integer daysUntilNextCount

    Boolean hasStockOnHandOrNegativeStock

    Date dateLatestInventory

    Integer sortOrder

    static constraints = {
        version false
        table "cycle_count_candidate"
    }
}
