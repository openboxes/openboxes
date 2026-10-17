package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class InventoryBrowserControllerSpec extends Specification implements ControllerUnitTest<InventoryBrowserController> {

    void 'index redirects to the list page which has no model'() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/inventoryBrowser/list'
        controller.list() == null
    }
}
