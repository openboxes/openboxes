package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class LocationsConfigurationControllerSpec extends Specification implements ControllerUnitTest<LocationsConfigurationController> {

    @Unroll
    void "#action renders the single page application"() {
        when:
        controller."$action"()

        then:
        view == "/common/react"

        where:
        action << ["index", "create", "upload"]
    }
}

class LoadDataControllerSpec extends Specification implements ControllerUnitTest<LoadDataController> {

    void "index renders the single page application"() {
        when:
        controller.index()

        then:
        view == "/common/react"
    }
}

class ResettingInstanceInfoControllerSpec extends Specification implements ControllerUnitTest<ResettingInstanceInfoController> {

    void "index renders the single page application"() {
        when:
        controller.index()

        then:
        view == "/common/react"
    }
}
