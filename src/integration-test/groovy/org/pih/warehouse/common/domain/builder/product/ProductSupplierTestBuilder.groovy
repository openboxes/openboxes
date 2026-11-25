package org.pih.warehouse.common.domain.builder.product

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier

@InheritConstructors
class ProductSupplierTestBuilder extends TestBuilder<ProductSupplier> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                code: randomUtil.randomStringFieldValue("code"),
                name: randomUtil.randomStringFieldValue("name"),
        ] as Map<String, Object>
    }

    ProductSupplierTestBuilder product(Product product) {
        args.product = product
        return this
    }
}
