package org.pih.warehouse.requisition

import grails.test.mixin.integration.Integration
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

@Integration
class RequisitionControllerIntegrationTests {

    def sessionFactory
    def dataService
    def inventoryService
    def requisitionService
    def productService


    @Test
    void list_shouldListRequisitions() {
        def location = Location.findOrCreateByName("Boston Headquarters").save(flush:true)
        def category = Category.findOrCreateByName("Medicine").save(flush:true)
        def product1 = Product.findOrCreateWhere(name: "Advil 200mg", category: category).save(flush:true)
        def product2 = Product.findOrCreateWhere(name: "Tylenol 325mg", category: category).save(flush:true)
        def item1 = new RequisitionItem(product: product1, quantity: 10)
        def item2 = new RequisitionItem(product: product2, quantity: 20)
        def person = Person.list().first()
        def requisition = new Requisition(
                name:'testRequisition'+ UUID.randomUUID().toString()[0..5],
                commodityClass: CommodityClass.MEDICATION,
                type:  RequisitionType.WARD_NON_STOCK,
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

        assert controller.modelAndView.viewName.contains("list")
        assert 1 == controller.modelAndView.model.requisitions.size()

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
