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

    @Test
    void list_shouldListRequisitions() {
        def location = Location.findByName("Boston Headquarters")
        def product1 = Product.findByName("Advil 200mg")
        def product2 = Product.findByName("Tylenol 325mg")
        def item1 = new RequisitionItem(product: product1, quantity: 10)
        def item2 = new RequisitionItem(product: product2, quantity: 20)
        def person = Person.list().first()
        def requisition = new Requisition(
                name:'testRequisition'+ UUID.randomUUID().toString()[0..5],
                commodityClass: CommodityClass.MEDICATION,
                type:  RequisitionType.NON_STOCK,
                origin: location,
                destination: location,
                requestedBy: person,
                dateRequested: new Date(),
                requestedDeliveryDate: new Date().plus(1))

        requisition.addToRequisitionItems(item1)
        requisition.addToRequisitionItems(item2)
        requisition.save(flush:true, failOnError: true)

        def controller = new RequisitionController();
        controller.session.warehouse = location
        controller.session.user = User.list().first()
        controller.list()

        assertTrue controller.modelAndView.viewName.contains("list")
        assertEquals 1, controller.modelAndView.model.requisitions.size()

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

        println "Response time: " + (System.currentTimeMillis() - startTime) + " ms"

        println sessionFactory.getStatistics()
    }
}
