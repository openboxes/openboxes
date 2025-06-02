package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product


class PendingCycleCountRequest {

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

    Map toJson() {
        return [
                id: id,
                facility: facility.toBaseJson(),
                cycleCountRequest: cycleCountRequest,
                product: [
                        id: product.id,
                        name: product.name,
                        productCode: product.productCode,
                ],
                category: [
                        id: product.category?.id,
                        name: product.category?.name,
                ],
                internalLocations: internalLocations,
                tags: tagsToJson(),
                productCatalogs: product.productCatalogs,
                abcClass: abcClass,
                quantityOnHand: quantityOnHand,
                quantityAllocated: quantityAllocated,
                status: status.toString(),
                negativeItemCount: negativeItemCount,
                requestType: requestType.toString(),
                blindCount: blindCount,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
        ]
    }

    List<Map> tagsToJson() {
        return product.tags?.collect { Tag tag ->
            [
                    id : tag.id,
                    tag: tag.tag,
            ]
        }
    }
}
