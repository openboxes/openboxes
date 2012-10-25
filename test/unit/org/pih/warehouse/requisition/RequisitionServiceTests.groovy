package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase


class RequisitionServiceTests extends GrailsUnitTestCase {
    void testSaveRequisition(){

        def requisitionMock = mockFor(Requisition)
        requisitionMock.demand.save{}
        def service = new RequisitionService()
        service.save(requisitionMock.createMock())

        requisitionMock.verify()
    }
}
