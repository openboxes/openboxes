package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierListParams

class ProductSupplierApiController {

    ProductSupplierService productSupplierService

    def list(ProductSupplierListParams filterParams) {
        List<ProductSupplier> productSuppliers = productSupplierService.getProductSuppliers(filterParams)
        render([data: productSuppliers.collect { it.toJson() }, totalCount: productSuppliers.totalCount] as JSON)
    }

    def delete() {
        productSupplierService.delete(params.id)
        render status: 204
    }
}
