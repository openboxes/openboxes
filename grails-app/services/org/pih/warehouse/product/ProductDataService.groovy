package org.pih.warehouse.product

import grails.gorm.services.Service

@Service(Product)
interface ProductDataService {

    void delete(String id)

    Product save(Product product)

    Product get(String id)
}
