package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.product.ProductPackageService
import org.pih.warehouse.product.ProductPackageCommand
import org.pih.warehouse.product.ProductSupplier

class ProductPackageApiController {

    ProductPackageService productPackageService

    def create(ProductPackageCommand productPackageCommand) {
        if (productPackageCommand.hasErrors()) {
            throw new ValidationException("Product package is invalid", productPackageCommand.errors)
        }
        ProductSupplier productSupplier = productPackageService.save(productPackageCommand)

        render([data: productSupplier] as JSON)
    }
}
