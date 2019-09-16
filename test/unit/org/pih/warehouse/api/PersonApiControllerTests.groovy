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
import org.pih.warehouse.core.Person

class PersonApiControllerTests extends ControllerUnitTestCase {
    Person person

    protected void setUp() {
        super.setUp()

        person = new Person(id: "person", firstName: "John", lastName: "Doe")

        mockDomain(Person, [person])
        mockConfig("openboxes.anonymize.enabled = false")
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testList() {
        // GIVEN
        controller.params.name = "John"
        controller.userService = [
                findPersons: { String[] terms -> return [person] }
        ]
        // WHEN
        controller.list()
        // THEN
        def response = controller.response.contentAsString
        assert response && response.size() > 0
        def jsonResponse = JSON.parse(response)
        assertEquals(jsonResponse.data.get(0).firstName, "John")
        assertEquals(jsonResponse.data.get(0).lastName, "Doe")
    }
}
