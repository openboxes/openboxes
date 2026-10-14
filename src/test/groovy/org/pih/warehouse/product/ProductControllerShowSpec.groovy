package org.pih.warehouse.product

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ProductControllerShowSpec extends Specification implements ControllerUnitTest<ProductController> {

    void "show redirects to the stock card of the product"() {
        given:
        params.id = 'abc123'

        when:
        controller.show()

        then:
        response.status == 302
        response.redirectedUrl == '/inventoryItem/showStockCard/abc123'
    }

    void "show does not forward other request params to the stock card"() {
        given:
        params.id = 'abc123'
        params.name = 'should not be forwarded'

        when:
        controller.show()

        then:
        response.redirectedUrl == '/inventoryItem/showStockCard/abc123'
    }

    void "show redirects to the stock card without an id when none is given"() {
        when:
        controller.show()

        then:
        response.status == 302
        response.redirectedUrl == '/inventoryItem/showStockCard'
    }
}
