package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

@Transactional
class ProductSupplierService {

    ProductSupplier saveProductSupplier(ProductSupplier productSupplier) {
        if (!productSupplier.hasErrors()) {
            return productSupplier.save()
        }
        return null
    }

    void deleteProductSupplier(ProductSupplier productSupplier) {
        productSupplier.delete()
    }
}
