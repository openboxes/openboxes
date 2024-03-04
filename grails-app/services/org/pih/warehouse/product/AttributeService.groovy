package org.pih.warehouse.product

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.EntityTypeCode

@Transactional
class AttributeService {

    List<Attribute> searchAttributes(String searchTerm, Map params) {
        return Attribute.createCriteria().list(params) {
            if (searchTerm) {
                ilike("name", "%" + searchTerm + "%")
            }
        }
    }

    def list(EntityTypeCode entityTypeCode) {
        String query = "select a from Attribute a"
        Map<String, String> argumentsList = [:]

        if (entityTypeCode) {
            query += " join a.entityTypeCodes etc where etc = :entityTypeCode"
            argumentsList += [entityTypeCode: entityTypeCode]
        }

        return Attribute.executeQuery(query, argumentsList)
    }
}
