package org.pih.warehouse.inventory

import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.core.validation.ObjectValidatable
import org.pih.warehouse.product.Product

/**
 * Identifies the product lot whose availability is being read.
 */
class LotAvailabilityCommand implements ObjectValidatable {

    Product product
    String lotNumber

    def beforeValidate() {
        Map<String, Object> params = RequestContextHolder.getRequestAttributes().params
        product = Product.read(params?.productId)
    }
}
