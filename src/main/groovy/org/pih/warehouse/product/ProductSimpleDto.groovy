package org.pih.warehouse.product

import org.pih.warehouse.core.dtos.IdentifiableDto

/**
 * A simplified representation of a Product, containing only the fields that are required
 * to display the product in its most basic form.
 */
class ProductSimpleDto implements IdentifiableDto {

    String productCode
    String name

    static ProductSimpleDto from(Product product) {
        return !product ? null : new ProductSimpleDto(
                id: product.id,
                productCode: product.productCode,
                name: product.name,
        )
    }
}
