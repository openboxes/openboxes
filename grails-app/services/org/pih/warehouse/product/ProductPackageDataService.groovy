package org.pih.warehouse.product

import grails.gorm.services.Service

@Service(value = ProductPackage, name = "productPackageGormService")
interface ProductPackageDataService {
    void delete(String id);
}
