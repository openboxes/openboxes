package org.pih.warehouse.api

import grails.converters.JSON
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.data.ProductSupplierService
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierFilterCommand

class ProductSupplierApiController {

    ProductSupplierService productSupplierService

    def list(ProductSupplierFilterCommand filterParams) {
        List<ProductSupplier> productSuppliers = productSupplierService.getProductSuppliers(filterParams)
        render([data: productSuppliers.collect { it.toJson() }, totalCount: productSuppliers.totalCount] as JSON)
    }

    def read() {
        ProductSupplier productSupplier = ProductSupplier.get(params.id)
        if (!productSupplier) {
            throw new ObjectNotFoundException(params.id, ProductSupplier.class.toString())
        }

        render([data: productSupplier] as JSON)
    }

    def delete() {
        productSupplierService.delete(params.id)
        render status: 204
    }
}
