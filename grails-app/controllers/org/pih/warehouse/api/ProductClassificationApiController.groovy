package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.product.ProductClassificationService

class ProductClassificationApiController {

    ProductClassificationService productClassificationService

    def list() {
        List<String> productClassifications = productClassificationService.list()
        render([data: productClassifications] as JSON)
    }
}
