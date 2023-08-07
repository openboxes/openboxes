package org.pih.warehouse.product

import grails.gorm.services.Join
import grails.gorm.services.Service

@Service(ProductType)
interface ProductTypeDataService {

    void delete(String id)

    ProductType save(ProductType product)

    ProductType get(String id)

    @Join("requiredFields")
    ProductType getWithRequiredFields(String id)

}
