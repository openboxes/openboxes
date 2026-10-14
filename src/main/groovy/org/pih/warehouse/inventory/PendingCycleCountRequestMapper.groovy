package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.serialization.SerializationMapper
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalogDto

@Component
class PendingCycleCountRequestMapper implements SerializationMapper<PendingCycleCountRequest> {

    @Override
    Map<String, Object> serialize(PendingCycleCountRequest source) {
        return [
                id: source.id,
                facility: source.facility?.id,
                cycleCountRequest: source.cycleCountRequest,
                product: [
                        id: source.product.id,
                        name: source.product.name,
                        productCode: source.product.productCode,
                ],
                category: [
                        id: source.product.category?.id,
                        name: source.product.category?.name,
                ],
                internalLocations: source.internalLocations,
                tags: serializeTags(source.product),
                productCatalogs: source.product.productCatalogs.collect { ProductCatalogDto.from(it) },
                abcClass: source.abcClass,
                quantityOnHand: source.quantityOnHand,
                quantityAllocated: source.quantityAllocated,
                status: source.status.toString(),
                negativeItemCount: source.negativeItemCount,
                requestType: source.requestType.toString(),
                blindCount: source.blindCount,
                dateCreated: source.dateCreated,
                lastUpdated: source.lastUpdated,
        ]
    }

    private List<Map> serializeTags(Product product) {
        return product.tags?.collect { Tag tag ->
            [
                    id : tag.id,
                    tag: tag.tag,
            ]
        }
    }
}
