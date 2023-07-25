package org.pih.warehouse.product

import grails.gorm.services.Service

@Service(ProductGroup)
interface ProductGroupDataService {

    void delete(String id)

    ProductGroup save(ProductGroup productGroup)

    ProductGroup get(String id)
}
