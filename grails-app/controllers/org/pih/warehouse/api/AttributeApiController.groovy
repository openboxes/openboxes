package org.pih.warehouse.api

import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.product.Attribute
import grails.converters.JSON
import org.pih.warehouse.product.AttributeService

class AttributeApiController {

    AttributeService attributeService

    def list() {
        EntityTypeCode entityTypeCode = params.get("entityType") as EntityTypeCode
        List<Attribute> attributes = attributeService.list(entityTypeCode)
        render([data: attributes] as JSON)
    }
}
