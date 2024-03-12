package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.product.ProductSupplierPreference
import org.pih.warehouse.product.ProductSupplierPreferenceBatchCommand
import org.pih.warehouse.product.ProductSupplierPreferenceService
import org.springframework.http.HttpStatus

class ProductSupplierPreferenceApiController {

    ProductSupplierPreferenceService productSupplierPreferenceService

    def create(ProductSupplierPreference productSupplierPreference) {
        if (productSupplierPreference.hasErrors()) {
            throw new ValidationException("Product supplier preference is invalid", productSupplierPreference.errors)
        }

        ProductSupplierPreference persistedProductSupplierPreference =
            productSupplierPreferenceService.save(productSupplierPreference)

        response.status = HttpStatus.CREATED.value()
        render([data: persistedProductSupplierPreference] as JSON)
    }

    def createOrUpdateBatch(ProductSupplierPreferenceBatchCommand command) {
        if (command.hasErrors()) {
            throw new ValidationException("Invalid product supplier preferences", command.errors)
        }

        List<ProductSupplierPreference> productSupplierPreferences =
            productSupplierPreferenceService.saveOrUpdateBatch(command.productSupplierPreferences)

        response.status = HttpStatus.OK.value()
        render([data: productSupplierPreferences] as JSON)
    }

    def update(ProductSupplierPreference productSupplierPreference) {
        if (productSupplierPreference.hasErrors()) {
            throw new ValidationException("Product supplier preference is invalid", productSupplierPreference.errors)
        }

        ProductSupplierPreference updatedProductSupplierPreference =
            productSupplierPreferenceService.save(productSupplierPreference)

        response.status = HttpStatus.OK.value()
        render([data: updatedProductSupplierPreference] as JSON)
    }

    def delete() {
        productSupplierPreferenceService.delete(params.id)

        render status: 204
    }
}
