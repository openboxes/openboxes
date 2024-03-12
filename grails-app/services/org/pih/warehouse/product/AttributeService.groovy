package org.pih.warehouse.product

import org.pih.warehouse.core.EntityTypeCode

class AttributeService {

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
