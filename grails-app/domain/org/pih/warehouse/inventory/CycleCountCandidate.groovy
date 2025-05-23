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

    Map toJson() {
        return [
                product: [
                        id: product.id,
                        name: product.name,
                        productCode: product.productCode,
                ],
                dateLastCount: dateLastCount,
                dateNextCount: dateNextCount,
                daysUntilNextCount: daysUntilNextCount,
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
                cycleCountRequest: cycleCountRequest,
                status: status.toString(),
                inventoryItemCount: inventoryItemCount,
                negativeItemCount: negativeItemCount,
                hasStockOnHandOrNegativeStock: hasStockOnHandOrNegativeStock,
                sortOrder: sortOrder,
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
