package org.pih.warehouse.api

import grails.converters.JSON

import org.pih.warehouse.product.ProductClassificationDto
import org.pih.warehouse.product.ProductClassificationService

class ProductClassificationApiController {

    ProductClassificationService productClassificationService

    def list() {
        List<ProductClassificationDto> productClassifications = productClassificationService.list(params.facilityId)
        render([data: productClassifications] as JSON)
    }
}
