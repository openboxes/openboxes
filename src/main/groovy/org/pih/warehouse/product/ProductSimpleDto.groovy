package org.pih.warehouse.product

import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * A simplified representation of a Product, containing only the fields that are required
 * to display the product in its most basic form.
 */
class ProductSimpleDto implements ResponseBodyFormattable {

    String id
    String productCode
    String name

    static ProductSimpleDto from(Product product) {
        return !product ? null : new ProductSimpleDto(
                id: product.id,
                productCode: product.productCode,
                name: product.name,
        )
    }

    @Override
    Map<String, Object> asResponseBody() {
        return [
                id: id,
                productCode: productCode,
                name: name,
        ]
    }
}
