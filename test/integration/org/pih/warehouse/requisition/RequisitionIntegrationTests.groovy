package org.pih.warehouse.requisition

import org.pih.warehouse.core.BaseIntegrationTest
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product

class RequisitionIntegrationTests extends GroovyTestCase {

    void test_RequisitionSaved() {

        def location = Location.list().first()
        def product1 = Product.findByName("Advil 200mg")
        def product2 = Product.findByName("Tylenol 325mg")
        def item1 = new RequisitionItem(product: product1, quantity: 10)
        def item2 = new RequisitionItem(product: product2, quantity: 20)
        def person = Person.list().first()
        def requisition = new Requisition(name:'testRequisition'+ UUID.randomUUID().toString()[0..5], origin: location, destination: location, requestedBy: person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1))
        requisition.addToRequisitionItems(item1)
        requisition.addToRequisitionItems(item2)

        assert requisition.status == RequisitionStatus.NEW
        requisition.status = RequisitionStatus.OPEN

        requisition.validate()
        requisition.errors.each{ println(it)}

        assert requisition.save(flush:true)


    }

    void test_saveRequisitionItemOnly(){
        def location = Location.list().first()
        def person = Person.list().first()
        def requisition = new Requisition(name:'testRequisition'+ UUID.randomUUID().toString()[0..5], origin: location, destination: location, requestedBy: person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1))

        assert requisition.save(flush:true)

        def product = Product.findByName("Advil 200mg")
        def item = new RequisitionItem(product: product, quantity: 10, requisition: requisition)
        assert item.save(flush:true)

    }

}
