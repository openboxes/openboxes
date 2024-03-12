package org.pih.warehouse.product

import grails.validation.Validateable
import org.pih.warehouse.core.EntityTypeCode

class ProductSupplierAttributeBatchCommand implements Validateable {

    List<ProductSupplierAttributeCommand> productAttributes

    static constraints = {
        productAttributes(validator: { List<ProductSupplierAttributeCommand> productAttributes ->
            // Elements of a list are not validated by default, so proceed manual validation of every element in the list
            productAttributes.each { ProductSupplierAttributeCommand command -> command.validate() }
            // If any of elements have validation errors, throw an exception
            if (productAttributes.any { it.hasErrors() }) {
                return false
            }

            // Search for required attributes and check if a user provided every required argument in the request
            List<Attribute> requiredAttributes =
                Attribute.findAll("from Attribute a where :entityTypeCodes in elements(a.entityTypeCodes) and a.required = 1", [entityTypeCodes: EntityTypeCode.PRODUCT_SUPPLIER])
            List<String> missingRequiredAttributes = requiredAttributes?.id - productAttributes?.attribute?.id
            // If missing required attributes list is not empty, throw an exception and show user what attributes are missing
            if (missingRequiredAttributes.size()) {
                return ['missing.required', missingRequiredAttributes.join(", ")]
            }
            return true
        })
    }
}
