package org.pih.warehouse.databinding

import org.springframework.stereotype.Component

import org.pih.warehouse.product.Product

/**
 * Binds a Product-typed field from a plain String that is either the Product's id or its productCode,
 * so callers can reference products by their natural key without first resolving the id.
 */
@Component
class ProductValueConverter extends StringValueConverter<Product> {

    @Override
    Product convertString(String value) {
        return Product.findByIdOrProductCode(value, value)
    }
}
