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
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

class LocationApiControllerTests extends ControllerUnitTestCase {
    def user
    def warehouse
    def location

    protected void setUp() {
        super.setUp()

        user = new User(id: "user", username: "JohnDoe", firstName: "John", lastName: "Doe")
        mockDomain(User, [user])

        warehouse = new Location(id: "warehouse")
        location = new Location(id: "loc", name: "Test")
        mockDomain(Location, [warehouse, location])

        controller.grailsApplication = new DefaultGrailsApplication()
        controller.grailsApplication.config.openboxes.typeahead.minLength = 3

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
        controller.params.name = "Test"
        controller.session.warehouse = warehouse
        controller.session.user = user
        controller.userService = [
                isSuperuser: { User user -> return true }
        ]
        controller.locationService = [
                getLocations: { String[] fields, Map params, Boolean isSuperuser, String direction, Location currentLocation, User u -> [location] }
        ]
        // WHEN
        controller.list()
        // THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        assertEquals(jsonResponse.data.get(0).name, "Test")
    }
}
