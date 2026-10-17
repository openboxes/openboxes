package org.pih.warehouse.regression.platform.shipping

import spock.lang.PendingFeature

import org.pih.warehouse.shipping.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ShipmentControllerAddEventSpec extends Specification implements ControllerUnitTest<ShipmentController>, DataTest {

    void setupSpec() {
        mockDomains(Shipment)
    }

    void setup() {
        Expando messages = new Expando()
        messages.message = { Map args -> 'message' }
        controller.metaClass.getWarehouse = { -> messages }
    }

    @PendingFeature(reason = 'OBAF addEvent has no return after the not-found redirect and renders too')
    void 'adding an event to an unknown shipment only redirects to the list'() {
        given:
        params.id = 'unknown-shipment'

        when:
        controller.addEvent()

        then:
        response.redirectedUrl == '/shipment/list'
        controller.modelAndView == null
    }
}
