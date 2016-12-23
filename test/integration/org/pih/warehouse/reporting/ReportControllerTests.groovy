package org.pih.warehouse.reporting

import org.junit.Test
import org.pih.warehouse.core.Location

class ReportControllerTests extends GroovyTestCase {

    @Test
    void testShowInventorySamplingReport() {
        def location = Location.findOrCreateWhere(name: "Boston Headquarters");
        assert location != null

        def controller = new ReportController()
        controller.session.warehouse = location
        controller.params.n = 4
        controller.showInventorySamplingReport()

        assert controller.response.contentAsString != null
        assert controller.response.contentAsString.split("\n").size() == 5
    }

    @Test
    void testShowInventorySamplingReport_shouldHandleErrorCase() {
        def location = Location.findOrCreateWhere(name: "Boston Headquarters");
        assert location != null

        def controller = new ReportController()
        controller.session.warehouse = location
        controller.showInventorySamplingReport()

        assert controller.response.contentAsString != null
        assert controller.response.contentAsString.contains("You cannot")
        //assert controller.response.contentAsString.split("\n").size() == 11
    }


}
