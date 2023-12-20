package org.pih.warehouse.product

import grails.gorm.transactions.Transactional

@Transactional
class AttributeService {

    List<Attribute> getAttributes(int offset, int max, String name) {
        return Attribute.createCriteria().list(offset: offset, max: max) {
            if (name) {
                ilike("name", "%" + name + "%")
            }
        }
    }
}
