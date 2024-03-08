package org.pih.warehouse.product

import grails.validation.Validateable
import org.pih.warehouse.core.EntityTypeCode

class ProductSupplierAttributeCommand implements Validateable {

    Attribute attribute

    ProductSupplier productSupplier

    String value

    static constraints = {
        attribute(validator: { Attribute attr, ProductSupplierAttributeCommand command ->
            // Attribute must have entity type code Product Supplier
            if (!command.attribute?.entityTypeCodes?.contains(EntityTypeCode.PRODUCT_SUPPLIER)) {
                return ['invalid', command.attribute?.name]
            }
            return true
        })
        value(validator: { String val, ProductSupplierAttributeCommand command ->
            // If value is empty and the attribute is required and active, throw an error
            // if value is empty and the attribute is not required, we are supposed to delete this attribute,
            // hence the validation should pass
            if (!val) {
                if (command.attribute?.required && command.attribute?.active) {
                    return ['required', command.attribute?.name]
                }
                return true
            }
            // If the attribute does not allow free text and given value is not in the list of available options, throw an error
            if (!command.attribute?.options?.contains(val) && !command.attribute?.allowOther) {
                return ['invalid', command.attribute?.name]
            }
            return true
        })
    }
}
