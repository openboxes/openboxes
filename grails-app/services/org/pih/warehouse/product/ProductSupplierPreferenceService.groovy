package org.pih.warehouse.product

import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

@Transactional
class ProductSupplierPreferenceService {

    // DataServices compile before the Spring context is initialized, so we lazy load them to avoid null pointers
    // when one exists down the chain from a Spring component (even when there's a Grails component in between).
    @Autowired
    @Lazy
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

    void delete(Set<ProductSupplierPreference> productSupplierPreferences) {
        productSupplierPreferences.each { delete(it.id) }
    }
}
