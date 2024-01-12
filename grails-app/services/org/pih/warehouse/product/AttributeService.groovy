package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

@Transactional
class AttributeService {

    List<Attribute> searchAttributes(String searchTerm, Map params) {
        return Attribute.createCriteria().list(params) {
            if (searchTerm) {
                ilike("name", "%" + searchTerm + "%")
            }
        }
    }
}
