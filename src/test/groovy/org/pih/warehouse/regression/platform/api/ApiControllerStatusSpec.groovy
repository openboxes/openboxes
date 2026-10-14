package org.pih.warehouse.regression.platform.api

import org.pih.warehouse.api.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.product.Product

class ApiControllerStatusSpec extends Specification implements ControllerUnitTest<ApiController>, DataTest {

    void setupSpec() {
        mockDomain(Product)
    }

    void cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(Product)
    }

    void '200 while the database answers'() {
        when:
        controller.status()

        then:
        response.status == 200
        response.json.status == 'OK'
        response.json.database.status == true
    }

}
