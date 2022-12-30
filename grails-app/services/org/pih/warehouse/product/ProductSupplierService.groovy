package org.pih.warehouse.product;

import grails.gorm.transactions.Transactional;

@Transactional
class ProductSupplierService {

    void deleteProductSupplier(ProductSupplier productSupplierInstance) {
        productSupplierInstance.delete(flush: true)
    }
}
