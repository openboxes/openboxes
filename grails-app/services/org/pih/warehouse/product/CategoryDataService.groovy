package org.pih.warehouse.product

import grails.gorm.services.Service

@Service(value = Category, name = "categoryGormService")
interface CategoryDataService {

    void delete(String id)

    Category save(Category category)

    Category get(String id)
}
