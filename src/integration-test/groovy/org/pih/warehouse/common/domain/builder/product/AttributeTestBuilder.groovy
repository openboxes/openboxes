package org.pih.warehouse.common.domain.builder.product

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.EntityTypeCode
import org.pih.warehouse.product.Attribute

@InheritConstructors
class AttributeTestBuilder extends TestBuilder<Attribute> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                code: randomUtil.randomStringFieldValue("code"),
                name: randomUtil.randomStringFieldValue("Attribute"),
                description: "Description",
                active: true,
                exportable: true,
                required: false,
                allowOther: false,
                allowMultiple: false,
        ] as Map<String, Object>
    }

    AttributeTestBuilder name(String name) {
        args.name = name
        return this
    }

    AttributeTestBuilder active(boolean active) {
        args.active = active
        return this
    }

    AttributeTestBuilder entityTypeCodes(EntityTypeCode... entityTypeCodes) {
        args.entityTypeCodes = entityTypeCodes as Set<EntityTypeCode>
        return this
    }
}
