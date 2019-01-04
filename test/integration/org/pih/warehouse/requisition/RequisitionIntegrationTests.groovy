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
        //assertTrue requisition.errors.hasFieldErrors("commodityClass")
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

//    Commented out because test not needed at this moment
//    void testGetPendingRequisitions() {
//        def person = Person.list().first()
//        def location = Location.list().first()
//        def location2 = Location.list().last()
//
//        def requisition1 = new Requisition(id:"requisition1", status: RequisitionStatus.CREATED,
//                origin: location, destination: location, name:"oldRequisition1",
//                description: "oldDescription1", requestedBy: person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1))
//        def requisition2 = new Requisition(id:"requisition2", status: RequisitionStatus.OPEN,
//                origin: location, destination: location, name:"oldRequisition2",
//                description: "oldDescription2", requestedBy: person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1))
//        def requisition3 = new Requisition(id:"requisition3", status: RequisitionStatus.OPEN,
//                origin: location2, destination: location2, name:"oldRequisition3",
//                description: "oldDescription3", requestedBy: person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1))
//        def requisition4 = new Requisition(id:"requisition4", status: RequisitionStatus.CANCELED,
//                origin: location, destination: location, name:"oldRequisition4",
//                description: "oldDescription4", requestedBy: person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1))
//
//        assert requisition1.save(flush:true)
//        assert requisition2.save(flush:true)
//        assert requisition3.save(flush:true)
//        assert requisition4.save(flush:true)
//
//        def service = new RequisitionService()
//        def collection = service.getPendingRequisitions(location)
//
//        assert collection.size() == 2
//        assert collection.find {it.id == requisition1.id}
//        assert collection.find {it.id == requisition2.id}
//        assert !collection.find {it.id == requisition3.id}
//        assert !collection.find {it.id == requisition4.id}
//
//    }

}
