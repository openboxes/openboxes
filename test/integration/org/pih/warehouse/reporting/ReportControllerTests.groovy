package org.pih.warehouse.reporting

import grails.converters.JSON
import grails.test.ControllerUnitTestCase
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.*
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.ReportController

class ReportControllerTests extends ControllerUnitTestCase {

    @Test
    void showInventorySamplingReport() {
        def location = Location.findByName("Boston Headquarters");
        assert location != null

        def controller = new ReportController()
        controller.session.warehouse = location
        controller.params.n = 4
        controller.showInventorySamplingReport()

        assert controller.response.contentAsString != null
        assert controller.response.contentAsString.split("\n").size() == 5
    }

    @Test
    void showInventorySamplingReport_shouldHandleErrorCase() {
        def location = Location.findByName("Boston Headquarters");
        assert location != null

        def controller = new ReportController()
        controller.session.warehouse = location
        controller.showInventorySamplingReport()

        assert controller.response.contentAsString != null
        assert controller.response.contentAsString.contains("You cannot")
        //assert controller.response.contentAsString.split("\n").size() == 11
    }


}
