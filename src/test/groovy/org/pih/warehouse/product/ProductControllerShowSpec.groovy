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

    void "show only passes the id to the stock card and drops other request params"() {
        given: "an unrelated request param next to the product id"
        params.id = 'abc123'
        params.name = 'should not be forwarded'

        when:
        controller.show()

        then: "the redirect URL has just the id, with no ?name=... query string"
        response.redirectedUrl == '/inventoryItem/showStockCard/abc123'
    }

    void "show redirects to the stock card without an id when none is given"() {
        // This only checks the redirect that show() itself issues. The stock card then cannot find the product, so
        // InventoryItemController.showStockCard sets a flash message and sends the user to the dashboard.
        when:
        controller.show()

        then:
        response.status == 302
        response.redirectedUrl == '/inventoryItem/showStockCard'
    }
}
