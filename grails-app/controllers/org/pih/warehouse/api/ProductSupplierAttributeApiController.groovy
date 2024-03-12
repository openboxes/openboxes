package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.product.ProductSupplierAttributeBatchCommand
import org.pih.warehouse.product.ProductSupplierAttributeCommand
import org.pih.warehouse.product.ProductSupplierAttributeService
import org.pih.warehouse.product.ProductSupplierAttributeUpdateResponse
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors

class ProductSupplierAttributeApiController {

    ProductSupplierAttributeService productSupplierAttributeService


    def updateAttributes(ProductSupplierAttributeBatchCommand command) {
        if (command.hasErrors()) {
            // Build errors manually to be able to include errors both for the batch command instance
            // and errors for every element of the productAttributes list
            Errors errors = new BeanPropertyBindingResult(command, "productAttributes")
            // Iterate every element of productAttributes and add its error to the errors instance.
            command.productAttributes.each { ProductSupplierAttributeCommand attribute ->
                attribute.errors.allErrors.each { error ->
                    errors.addError(error)
                }
            }
            // If there are not any errors in the elements, it means that there must be errors in the batch command instance
            // e.g. missing required arguments
            if (!errors.hasErrors()) {
                command.errors.allErrors.each { error ->
                    errors.addError(error)
                }
            }
            throw new ValidationException("Product attributes are invalid", errors)
        }
        ProductSupplierAttributeUpdateResponse response = productSupplierAttributeService.updateAttributes(command)

        render([data: response] as JSON)
    }
}
