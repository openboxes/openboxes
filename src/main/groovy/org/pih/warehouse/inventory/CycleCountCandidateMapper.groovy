package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.mapper.ResponseMapper

@Component
class CycleCountCandidateMapper extends ResponseMapper<CycleCountCandidate> {

    @Override
    Map<String, Object> asResponseBody(CycleCountCandidate source) {
        return [
                product: [
                        id: source.product.id,
                        name: source.product.name,
                        productCode: source.product.productCode,
                ],
                dateLastCount: source.dateLastCount,
                dateNextCount: source.dateNextCount,
                daysUntilNextCount: source.daysUntilNextCount,
                category: [
                        id: source.product.category?.id,
                        name: source.product.category?.name,
                ],
                internalLocations: source.internalLocations,
                tags: tagsAsResponseBody(source.product.tags),
                productCatalogs: source.product.productCatalogs,
                abcClass: source.abcClass,
                quantityOnHand: source.quantityOnHand,
                quantityAllocated: source.quantityAllocated,
                cycleCountRequest: source.cycleCountRequest,
                status: source.status.toString(),
                inventoryItemCount: source.inventoryItemCount,
                negativeItemCount: source.negativeItemCount,
                hasStockOnHandOrNegativeStock: source.hasStockOnHandOrNegativeStock,
                sortOrder: source.sortOrder,
        ]
    }

    private List<Map> tagsAsResponseBody(Collection<Tag> tags) {
        return tags?.collect { Tag tag ->
            [
                    id : tag.id,
                    tag: tag.tag,
            ]
        }
    }

    @Override
    Map<String, Object> asExportRow(CycleCountCandidate object) {
        // TODO: will be implemented after refactoring export/import flow
        return null
    }
}
