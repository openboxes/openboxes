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
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductController
import spock.lang.Specification


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(ApiController)
@Mock(Product)
@TestMixin(GrailsUnitTestMixin)
class ApiControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "list all products"() {
        given:
        controller.products()
        log.info "model: " + model
        println "model: " + model
        println response
        println controller.response.text
        def json = JSON.parse(controller.response.contentAsString)

        expect:
        json.products.size() == 0
        //expect:
        //model.products.size() == 0
        //model.products == 0
    }

}