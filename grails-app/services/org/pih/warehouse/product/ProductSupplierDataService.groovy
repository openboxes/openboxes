package org.pih.warehouse.product

import grails.gorm.services.Service

@Service(value = ProductSupplier, name = "productSupplierGormService")
interface ProductSupplierDataService {
    void delete(String id);

    ProductSupplier save(ProductSupplier productSupplier);

    ProductSupplier get(String id);
}
