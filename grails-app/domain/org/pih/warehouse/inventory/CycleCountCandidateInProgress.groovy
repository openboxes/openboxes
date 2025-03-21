package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product


class CycleCountCandidateInProgress {

    String id

    CycleCountRequest cycleCountRequest

    Location facility

    Product product

    CycleCountCandidateStatus status

    CycleCount cycleCount

    String abcClass

    CycleCountRequestType requestType

    Boolean blindCount

    Date dateCreated

    Date lastUpdated

    User createdBy

    User updatedBy

    Integer quantityOnHand

    String internalLocations

    Integer negativeItemCount

    static mapping = {
        table "cycle_count_candidate_in_progress"
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
