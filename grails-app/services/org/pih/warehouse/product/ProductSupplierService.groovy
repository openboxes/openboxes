package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

@Transactional
class ProductSupplierService {

    ProductSupplier saveProductSupplier(ProductSupplier productSupplier) {
        return productSupplier.save()
    }
}
