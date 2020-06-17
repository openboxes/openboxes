package org.pih.warehouse.api

import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionController
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionType

class ApiControllerIntegrationTests extends GroovyTestCase {

    @Test
    void status_shouldDoSomething() {
        def location = Location.findByName("Boston Headquarters")
        def controller = new ApiController();
        controller.session.warehouse = location
        controller.session.user = User.list().first()
        controller.status()

        assertEquals 200, controller.response.status
        assertEquals "application/json;charset=UTF-8", controller.response.contentType
        assertEquals "{\"status\":\"OK\",\"database\":{\"status\":true,\"message\":\"Database is available\"}}", controller.response.contentAsString
    }

    @Test
    void renderDate_shouldRenderDatetime() {
        def location = Location.findByName("Boston Headquarters")
        def controller = new ApiController();
        controller.session.warehouse = location
        controller.session.user = User.list().first()
        controller.params.datetime = "01/01/2020"
        controller.datetime()
        assertEquals 200, controller.response.status
        //assertEquals "application/json;charset=UTF-8", controller.response.contentType
        println controller.response.contentAsString
    }

}
