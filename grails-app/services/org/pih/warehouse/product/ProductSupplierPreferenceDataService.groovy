package org.pih.warehouse.product

import grails.gorm.services.Service

@Service(ProductSupplierPreference)
interface ProductSupplierPreferenceDataService {
    void delete(String id)
}
