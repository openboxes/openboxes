package org.pih.warehouse.product

import org.pih.warehouse.core.EntityTypeCode

class AttributeService {

    def list(EntityTypeCode entityTypeCode, Boolean active = true) {
        String query = "select a from Attribute a"
        Map<String, String> argumentsList = [:]

        if (entityTypeCode) {
            query += " join a.entityTypeCodes etc where etc = :entityTypeCode and active = :active"
            argumentsList += [entityTypeCode: entityTypeCode, active: active]
        }

        return Attribute.executeQuery(query, argumentsList)
    }
}
