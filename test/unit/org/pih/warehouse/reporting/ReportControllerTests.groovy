package org.pih.warehouse.reporting

import grails.buildtestdata.mixin.Build
import grails.converters.JSON
import grails.test.ControllerUnitTestCase
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.reporting.ReportController

@TestFor(ReportController)
@Mock(Location)
@TestMixin(GrailsUnitTestMixin)
@Build(Location)
class ReportControllerTests {

    @Test
    void showInventorySamplingReport() {
        def location = Location.findOrCreateWhere(name: "Boston Headquarters");
        assert location != null


        controller.session.warehouse = location
        controller.params.n = 4
        controller.showInventorySamplingReport()

        assert controller.response.contentAsString != null
        assert controller.response.contentAsString.split("\n").size() == 5
    }

    @Test
    void showInventorySamplingReport_shouldHandleErrorCase() {
        def location = Location.findOrCreateWhere(name: "Boston Headquarters");
        assert location != null

        controller.session.warehouse = location
        controller.showInventorySamplingReport()

        assert controller.response.contentAsString != null
        assert controller.response.contentAsString.contains("You cannot")
        //assert controller.response.contentAsString.split("\n").size() == 11
    }


}
