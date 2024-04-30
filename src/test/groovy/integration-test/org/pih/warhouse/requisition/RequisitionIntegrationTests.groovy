package org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import spock.lang.Specification
import testutils.DbHelper
import static org.junit.Assert.*;

//@Ignore
class RequisitionIntegrationTests extends Specification implements DomainUnitTest<Requisition> {

    def sessionFactory


    @Before
    void setup() {
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
        when:
        def requisition = new Requisition()
        requisition.save()
        then:
        assertTrue requisition.hasErrors()
        println requisition.errors
        assertEquals 4, requisition.errors.errorCount
        assertTrue requisition.errors.hasFieldErrors("destination")
        assertTrue requisition.errors.hasFieldErrors("origin")
        assertTrue requisition.errors.hasFieldErrors("requestedBy")
    }

    @Test
    void save_shouldSaveRequisition() {
        when:
        def location = Location.list().first()
        def product1 = DbHelper.findOrCreateProduct('Advil 200mg')
        def product2 = DbHelper.findOrCreateProduct('Tylenol 325mg')
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

        then:
        assert requisition.save(flush:true)
    }

    @Test
    void save_shouldSaveRequisitionItemOnly() {
        when:
        def person = DbHelper.findOrCreateUser('Axl', 'Rose', 'axl@hotmail.com', 'axl', 'Sw337_Ch1ld', false)
        def requisition = new Requisition(
                name:'testRequisition'+ UUID.randomUUID().toString()[0..5],
                commodityClass: CommodityClass.MEDICATION,
                type:  RequisitionType.NON_STOCK,
                origin: DbHelper.findOrCreateLocation('somewhere over the rainbow'),
                destination: DbHelper.findOrCreateLocation('where the buffalo roam'),
                requestedBy: person,
                dateRequested: new Date(),
                requestedDeliveryDate: new Date().plus(1))

        then:
        assert requisition.save(flush:true)

        when:
        def product = DbHelper.findOrCreateProduct('Advil 200mg')
        def item = new RequisitionItem(product: product, quantity: 10, requestedBy: person)
        requisition.addToRequisitionItems(item)

        then:
        assert requisition.save(flush: true)
        assertEquals 1, requisition.requisitionItems.size()
    }

    /**
     * Temporary unit test created for performance tuning that should be @Ignored when committed to github.
     */
    @Ignore
    void getStockRequisition() {
        when:
        def startTime = System.currentTimeMillis()

        def requisition = Requisition.findByRequestNumber("508BSK")
        requisition.requisitionItems.each { requisitionItem ->
            println " * " + requisitionItem.toJson()
            requisitionItem.requisitionItems.each { grandchild ->
                println " \t* " + grandchild.toJson()
            }
        }
        then:
        assertNotNull(requisition)

        println "Response time: " + (System.currentTimeMillis() - startTime) + " ms"

    }
}
