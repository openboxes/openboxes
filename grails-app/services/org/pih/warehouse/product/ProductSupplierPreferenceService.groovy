package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

@Transactional
class ProductSupplierPreferenceService {

    ProductSupplierPreferenceDataService productSupplierPreferenceDataService

    ProductSupplierPreference save(ProductSupplierPreference productSupplierPreference) {
        // Add the product supplier preference instance to the productSupplier's preferences collection (OneToMany bidirectional)
        productSupplierPreference?.productSupplier?.addToProductSupplierPreferences(productSupplierPreference)
        return productSupplierPreference.save(flush: true, failOnError: true)
    }

    List<ProductSupplierPreference> saveOrUpdateBatch(List<ProductSupplierPreference> productSupplierPreferences) {
        List<ProductSupplierPreference> persistedProductSupplierPreferences = new ArrayList<>()
        productSupplierPreferences.each { ProductSupplierPreference productSupplierPreference ->
            ProductSupplierPreference persistedProductSupplierPreference = save(productSupplierPreference)
            persistedProductSupplierPreferences.add(persistedProductSupplierPreference)
        }
        return persistedProductSupplierPreferences
    }

    void delete(String id) {
        productSupplierPreferenceDataService.delete(id)
    }
}
