package org.pih.warehouse.product

import org.pih.warehouse.core.EntityTypeCode

class AttributeService {

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
