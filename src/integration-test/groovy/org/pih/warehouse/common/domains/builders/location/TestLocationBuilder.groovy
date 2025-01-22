package org.pih.warehouse.common.domains.builders.location

import org.pih.warehouse.common.domains.builders.location.base.TestBuilder
import org.pih.warehouse.core.Location

class TestLocationBuilder extends TestBuilder<Location> {

    TestLocationBuilder(Map<String, Object> args=[:]) {
        super(Location.class, args)
    }

    TestLocationBuilder facilityLocation() {
        // TODO: set some reasonable defaults here that a warehouse would have, such as an inventory
        return this
    }

    TestLocationBuilder binLocation() {
        // TODO: set some reasonable defaults here that a bin location would have
        return this
    }
}
