/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class EventTypeControllerTests extends Specification implements ControllerUnitTest<EventTypeController>, DataTest {

    Class[] getDomainClassesToMock() { [EventType] }

    def stubMessager = new Expando()

    void "test saving valid EventType"() {
        when:
        stubMessager.message = { args -> return "success" }
        controller.metaClass.warehouse = stubMessager
        controller.params.name = "testEvent"
        controller.params.eventCode = EventCode.SCHEDULED
        request.method = "POST"
        controller.save()

        then:
        response.redirectedUrl.startsWith('/eventType/list/')
        flash.message != null
        EventType.count() == 1
    }
}
