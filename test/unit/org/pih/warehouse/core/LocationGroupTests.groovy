/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.core

import grails.test.*
import org.junit.Ignore
import org.junit.Test

class LocationGroupTests extends GrailsUnitTestCase {
    def bos = new LocationGroup(name: "Boston")
    def pap = new LocationGroup(name: "Port au Prince")
    def hum = new LocationGroup(name: "HUM")

    protected void setUp() {
        super.setUp()
        mockDomain(LocationGroup, [bos, pap, hum])
    }

    @Ignore
    void validate() {
        def locationGroup = new LocationGroup()
        assertFalse locationGroup.validate()
        assertEquals "nullable", locationGroup.errors["name"]
    }

    @Test
    void compareTo_shouldSortByName() {
        def locations = [pap, bos, hum]
        locations = locations.sort()
        assertEquals locations[0].name, "Boston"
        assertEquals locations[1].name, "HUM"
        assertEquals locations[2].name, "Port au Prince"
    }
}
