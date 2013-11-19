package org.pih.warehouse.requisition

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class RequisitionControllerIntegrationTests extends GroovyTestCase {

    def sessionFactory
    def dataService
    def inventoryService
    def requisitionService
    def productService

    @Before
    void setUp() {
        assertNotNull sessionFactory
        println sessionFactory.getStatistics()
    }

    @After
    void tearDown() {
        assertNotNull sessionFactory
        println sessionFactory.getStatistics()
    }




    /**
     * Temporary unit test created for performance tuning that should be @Ignored when committed to github.
     */
    @Ignore
    void reviewRequisition() {

        println sessionFactory.getStatistics()
        def startTime = System.currentTimeMillis()
        def controller = new RequisitionController()

        controller.requisitionService = requisitionService
        controller.inventoryService = inventoryService
        controller.dataService = dataService
        controller.productService = productService
        controller.session.warehouse = Location.get("c879370c3d35752b013d3b6c983c030f")
        controller.session.user = User.get("3")

        def params = [id: "c879370c414a9a5601414ae71fde0001"]
        controller.params.putAll(params)
        controller.review()

        println controller.response

        //println controller.renderArgs
        //println controller.modelAndView.view
        //println controller.modelAndView.viewName
        //println controller.modelAndView.model
        //println controller.modelAndView.modelMap


        println "Response time: " + (System.currentTimeMillis() - startTime) + " ms"

        println sessionFactory.getStatistics()
        //def requisition = Requisition.findByRequestNumber("508BSK")
        //assertEquals controller.modelAndView.model.requisition, requisition
        //assertEquals controller.modelAndView.viewName, "/requisition/review"



    }


}
