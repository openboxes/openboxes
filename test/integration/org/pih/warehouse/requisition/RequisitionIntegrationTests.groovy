package org.pih.warehouse.requisition

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product

class RequisitionIntegrationTests extends GroovyTestCase {

    def sessionFactory


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
    void save_shouldReturnErrors() {
        def requisition = new Requisition()
        requisition.save()
        assertTrue requisition.hasErrors()
        println requisition.errors
        assertEquals 3, requisition.errors.errorCount
        assertTrue requisition.errors.hasFieldErrors("destination")
        assertTrue requisition.errors.hasFieldErrors("origin")
        assertTrue requisition.errors.hasFieldErrors("requestedBy")
    }

    @Test
    void save_shouldSaveRequisition() {
        def location = Location.list().first()
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

        requisition.validate()
        requisition.errors.each{ println(it)}

        assert requisition.save(flush:true)


    }

    @Test
    void save_shouldSaveRequisitionItemOnly(){
        def location = Location.list().first()
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

        assert requisition.save(flush:true)

        def product = Product.findByName("Advil 200mg")
		def item = new RequisitionItem(product: product, quantity: 10)
		requisition.addToRequisitionItems(item);
		
		assert requisition.save(flush:true)
		assertEquals 1, requisition.requisitionItems.size()

    }

    /**
     * Temporary unit test created for performance tuning that should be @Ignored when committed to github.
     */
    @Ignore
    void getStockRequisition() {
        def startTime = System.currentTimeMillis()

        def requisition = Requisition.findByRequestNumber("508BSK")
        requisition.requisitionItems.each { requisitionItem ->
            println " * " + requisitionItem.toJson()
            requisitionItem.requisitionItems.each { grandchild ->
                println " \t* " + grandchild.toJson()
            }
        }


        println "Response time: " + (System.currentTimeMillis() - startTime) + " ms"

    }
}
