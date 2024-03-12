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

    def list(EntityTypeCode entityTypeCode, Boolean active = true) {
        String query = "select a from Attribute a"
        String whereQuery = " where a.active = :active"
        Map<String, String> argumentsList = [:]

        if (entityTypeCode) {
            argumentsList += [entityTypeCode: entityTypeCode]
            query += " join a.entityTypeCodes etc"
            whereQuery += " and etc = :entityTypeCode"
        }
        argumentsList += [active: active]
        query += whereQuery

        return Attribute.executeQuery(query, argumentsList)
    }
}
