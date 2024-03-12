package org.pih.warehouse.product

import grails.validation.Validateable

class ProductSupplierPreferenceBatchCommand implements Validateable {

    List<ProductSupplierPreference> productSupplierPreferences

    static constraints = {
        productSupplierPreferences(validator: { List<ProductSupplierPreference> productSupplierPreferences ->
            productSupplierPreferences.every { ProductSupplierPreference productSupplierPreference ->
                // First of all proceed with basic validation
                if (!productSupplierPreference?.validate()) {
                    return false
                }
                // Validation for default preference - there can be only one default (destinationParty == null)
                // If we create or edit a preference, and we already have a default preference for the product supplier
                // throw validation error
                Set<ProductSupplierPreference> existingPreferences = productSupplierPreference?.productSupplier?.productSupplierPreferences
                if (!productSupplierPreference?.destinationParty && existingPreferences?.any {
                    !it.destinationParty && it.id != productSupplierPreference?.id }) {
                        return false
                }
                return true
            }
        })
    }
}
