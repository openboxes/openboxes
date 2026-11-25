package org.pih.warehouse.common.domain.builder.core

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.PreferenceType

@InheritConstructors
class PreferenceTypeTestBuilder extends TestBuilder<PreferenceType> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: randomUtil.randomStringFieldValue("name"),
        ] as Map<String, Object>
    }
}
