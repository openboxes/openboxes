package org.pih.warehouse.common.domain.builder.core

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.LocationGroup

@InheritConstructors
class LocationGroupTestBuilder extends TestBuilder<LocationGroup> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: "Test Location Group ${randomUtil.randomStringFieldValue('suffix')}",
        ] as Map<String, Object>
    }

    LocationGroupTestBuilder name(String name) {
        args.name = name
        return this
    }
}
