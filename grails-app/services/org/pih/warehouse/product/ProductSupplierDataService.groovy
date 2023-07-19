package org.pih.warehouse.product

import grails.gorm.services.Join
import grails.gorm.services.Service


@Service(value = ProductSupplier, name = "productSupplierGormService")
interface ProductSupplierDataService {
    void delete(String id);

    ProductSupplier save(ProductSupplier productSupplier);

    // TO BE REMOVED LATER
    @Join("product")
    List<ProductSupplier> list();

    List<ProductSupplier> findByCodeAndProductCode(String code, String productCode);

    List<ProductSupplier> findProductSuppliers(String code, String productCode);
}
