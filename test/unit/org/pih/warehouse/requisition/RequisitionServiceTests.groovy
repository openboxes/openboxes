package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase


class RequisitionServiceTests extends GrailsUnitTestCase {
    void testSaveRequisition(){

        def requisitionMock = mockFor(Requisition)
        requisitionMock.demand.save{ true }
        def service = new RequisitionService()
        assert service.saveRequisition(requisitionMock.createMock())

        requisitionMock.verify()
    }

    void testSaveRequisitionItem(){
        def requisitionMock = mockFor(RequisitionItem)
        requisitionMock.demand.save{ true }
        def service = new RequisitionService()
        assert service.saveRequisitionItem(requisitionMock.createMock())

        requisitionMock.verify()
    }
}

