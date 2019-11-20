/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.test.ControllerUnitTestCase
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode

class BinLocationApiControllerTests extends ControllerUnitTestCase {
    Location bin
    Location location

    protected void setUp() {
        super.setUp()

        LocationType binLocationType = new LocationType(id: "bin", name: "Bin", locationTypeCode: LocationTypeCode.BIN_LOCATION)
        LocationType depotType = new LocationType(id: "depot", name: "Depot")
        LocationGroup boston = new LocationGroup(id: "boston", name: "Boston")

        mockDomain(LocationType, [binLocationType, depotType])
        mockDomain(LocationGroup, [boston])

        bin = new Location(id: "binLocationId", name: "TestBin", locationType: binLocationType, locationGroup: boston)
        location = new Location(id: "locationId", name: "Boston", locationType: depotType, locationGroup: boston, locations: [bin])

        mockDomain(Location, [bin, location])

        JSON.registerObjectMarshaller(Location) { Location location ->
            [
                    id                   : location.id,
                    name                 : location.name,
                    description          : location.description,
                    locationNumber       : location.locationNumber,
                    locationGroup        : location.locationGroup,
                    parentLocation       : location.parentLocation,
                    locationType         : location.locationType,
                    sortOrder            : location.sortOrder,
                    hasBinLocationSupport: location.hasBinLocationSupport()
            ]
        }
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testList() {
        // GIVEN
        controller.params.parentLocation = location
        // WHEN
        controller.list()
        // THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        println(jsonResponse.data)
    }

    void testRead() {
        // GIVEN
        controller.params.id = "locationId"
        // WHEN
        controller.read()
        // THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        println(jsonResponse.data)
    }
}
