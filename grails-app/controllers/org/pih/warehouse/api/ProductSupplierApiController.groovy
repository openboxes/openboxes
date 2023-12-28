package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.product.ProductSupplierListDto
import org.pih.warehouse.product.ProductSupplierListParams

class ProductSupplierApiController {

    ProductSupplierService productSupplierService

    def list(ProductSupplierListParams filterParams) {
        List<ProductSupplierListDto> productSuppliers = productSupplierService.getProductSuppliers(filterParams)
        render([data: productSuppliers, totalCount: productSuppliers.totalCount] as JSON)
    }
}
