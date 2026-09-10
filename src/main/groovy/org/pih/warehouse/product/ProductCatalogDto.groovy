package org.pih.warehouse.product

import org.pih.warehouse.core.dtos.IdentifiableDto

class ProductCatalogDto implements IdentifiableDto {

    String code
    String name
    String description
    Boolean active
    String color

    static ProductCatalogDto from(ProductCatalog catalog) {
        return new ProductCatalogDto(
                id : catalog.id,
                code: catalog.code,
                name: catalog.name,
                description: catalog.description,
                active: catalog.active,
                color: catalog.color,
        )
    }
}
