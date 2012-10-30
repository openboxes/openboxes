package org.pih.warehouse.requisition

import org.pih.warehouse.core.BaseIntegrationTest
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

class RequisitionIntegrationTests extends GroovyTestCase {

    void test_RequisitionSaved() {

        def location = Location.list().first()
        def person = Person.list().first()
        def requisition = new Requisition(name:'testRequisition', origin: location, destination: location, requestedBy: person, dateRequested: new Date(), requestedDeliveryDate: new Date().plus(1))
        assert requisition.status == RequisitionStatus.NEW
        requisition.status = RequisitionStatus.OPEN
        requisition.validate()
        requisition.errors.each{ println(it)}

        assert requisition.save(flush:true)



        def requesitionFromDB = Requisition.get(requisition.id)

        assertEquals(requisition.name, requesitionFromDB.name)
        assert requesitionFromDB.status == RequisitionStatus.OPEN

    }

}
