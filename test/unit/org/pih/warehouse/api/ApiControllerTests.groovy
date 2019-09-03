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
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User

class ApiControllerTests extends ControllerUnitTestCase {

    User user = new User(username: "John", password: "password", passwordConfirm: "password", firstName: "Test", lastName: "User",)
    Locale localeEn = new Locale("en", "US")
    Locale localeFr = new Locale("fr")
    LocationType depot = new LocationType(id: "1", name: "Depot")
    LocationGroup boston = new LocationGroup(id: "boston", name: "Boston")
    Location location = new Location(id: "locationId", name: "Boston", locationType: depot, locationGroup: boston)

    protected void setUp() {
        super.setUp()

        mockDomain(User, [user])
        mockDomain(Location, [location])
        controller.grailsApplication = new DefaultGrailsApplication()
        controller.grailsApplication.metadata.'app.revisionNumber' = "123"
        controller.grailsApplication.metadata.'app.buildDate' = "01/01/2019"
        controller.grailsApplication.metadata.'app.branchName' = "develop"
        controller.grailsApplication.metadata.'app.grails.version' = "1.3.9"
        controller.grailsApplication.metadata.'app.version' = "0.8.9"
        controller.grailsApplication.config.openboxes.megamenu = "test"

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

    void testLogin() {
        // GIVEN
        controller.userService = [
                authenticate: { String username, String password -> return true }
        ]
        controller.request.method = 'POST'
        controller.request.content = '{ "username" : "John", "password" : "password" }'
        // WHEN
        controller.login()
        // THEN
        assertEquals(200, controller.response.status)
    }

    void testChooseLocation() {
        // GIVEN
        controller.request.method = 'POST'
        controller.params.id = "locationId"
        controller.session.user = "John"
        // WHEN
        controller.chooseLocation()
        // THEN
        assertEquals(200, controller.response.status)
        assertEquals("User John is now logged into Boston", controller.response.contentAsString)
    }

    void testChooseLocale() {
        // GIVEN
        controller.request.method = 'POST'
        controller.params.id = "fr"
        controller.session.user = user
        controller.session.locale = localeEn
        controller.localizationService = [
                getLocale: { String id -> return localeFr }
        ]
        // WHEN
        controller.chooseLocale()
        // THEN
        assertEquals(200, controller.response.status)
        assertEquals("Current language is fr", controller.response.contentAsString)
    }

    void testGetAppContext() {
        // GIVEN
        controller.userService = [
                isSuperuser: { User user -> return false },
                isUserAdmin: { User user -> return true }
        ]

        controller.localizationService = [
                getCurrentLocale: { -> return localeEn }
        ]
        controller.session.user = user
        controller.session.warehouse = location
        controller.session.impersonateUserId = null
        controller.session.hostname = "Unknown"
        // WHEN
        controller.getAppContext()
        // THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        println(jsonResponse.data)
        assertEquals("John", jsonResponse.data.user.username)
        assertEquals("Boston", jsonResponse.data.location.name)
        assertEquals(false, jsonResponse.data.isSuperuser)
        assertEquals(true, jsonResponse.data.isUserAdmin)
        assertEquals("0.8.9", jsonResponse.data.appVersion)
        assertEquals("develop", jsonResponse.data.branchName)
        assertEquals("test", jsonResponse.data.environment)
    }
}
