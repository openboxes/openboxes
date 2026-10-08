package org.pih.warehouse.common.domain.builder.core

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.UnitOfMeasureType

@InheritConstructors
class UnitOfMeasureTestBuilder extends TestBuilder<UnitOfMeasure> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: randomUtil.randomStringFieldValue("name"),
                code: randomUtil.randomStringFieldValue("code"),
        ] as Map<String, Object>
    }

    UnitOfMeasureTestBuilder type(UnitOfMeasureType type) {
        args.uomClass = UnitOfMeasureClass.findByType(type)
        return this
    }
}
