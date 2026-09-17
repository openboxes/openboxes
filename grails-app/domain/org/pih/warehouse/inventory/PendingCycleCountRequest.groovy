package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.core.serialization.Serializable
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalogDto

class PendingCycleCountRequest implements Serializable<PendingCycleCountRequestMapper> {

    String id

    CycleCountRequest cycleCountRequest

    Location facility

    Product product

    CycleCountCandidateStatus status

    String abcClass

    CycleCountRequestType requestType

    Boolean blindCount

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    Integer quantityOnHand

    Integer quantityAllocated

    String internalLocations

    Integer negativeItemCount

    static mapping = {
        table "pending_cycle_count_request"
        version false
    }
}
