package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.serialization.SerializationMapper
import org.pih.warehouse.product.ProductCatalogDto

@Component
class CycleCountCandidateMapper implements SerializationMapper<CycleCountCandidate> {

    @Override
    Map<String, Object> serialize(CycleCountCandidate source) {
        return [
                product: [
                        id: source.product?.id,
                        name: source.product?.name,
                        productCode: source.product?.productCode,
                ],
                dateLastCount: source.dateLastCount,
                dateNextCount: source.dateNextCount,
                daysUntilNextCount: source.daysUntilNextCount,
                category: [
                        id: source.product?.category?.id,
                        name: source.product?.category?.name,
                ],
                internalLocations: source.internalLocations,
                tags: serializeTags(source.product?.tags),
                productCatalogs: source.product?.productCatalogs?.collect { ProductCatalogDto.from(it) },
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

    private List<Map> serializeTags(Collection<Tag> tags) {
        return tags?.collect { Tag tag ->
            [
                    id : tag.id,
                    tag: tag.tag,
            ]
        }
    }
}
