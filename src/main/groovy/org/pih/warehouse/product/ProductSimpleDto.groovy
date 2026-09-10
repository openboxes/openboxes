package org.pih.warehouse.product

/**
 * A simplified representation of a Product, containing only the fields that are required
 * to display the product in its most basic form.
 */
class ProductSimpleDto {

    String id
    String productCode
    String name

    List<ProductHandlingIconDto> handlingIcons = []

    static ProductSimpleDto from(Product product) {
        return !product ? null : new ProductSimpleDto(
                id: product.id,
                productCode: product.productCode,
                name: product.name,
                handlingIcons: ProductHandlingIconDto.from(product),
        )
    }
}
