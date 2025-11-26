/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package unit.org.pih.warehouse.core

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.LocationGroup

class LocationGroupSpec extends Specification implements DomainUnitTest<LocationGroup> {

    void 'validate should work as expected'() {
        given:
        LocationGroup locationGroup = new LocationGroup()

        expect:
        assert locationGroup.validate()
    }

    void 'sort should correctly sort a list of location groups'() {
        given:
        LocationGroup bos = new LocationGroup(name: "Boston")
        LocationGroup pap = new LocationGroup(name: "Port au Prince")
        LocationGroup hum = new LocationGroup(name: "HUM")
        List<LocationGroup> locations = [pap, bos, hum]

        when:
        locations = locations.sort()

        then:
        assert locations[0].name == "Boston"
        assert locations[1].name == "HUM"
        assert locations[2].name == "Port au Prince"
    }
}
