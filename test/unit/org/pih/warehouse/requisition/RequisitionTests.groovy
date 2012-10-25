package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase


class RequisitionTests extends GrailsUnitTestCase {

    void testConstraints() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition()
        assertFalse requisition.validate()
        assertEquals "nullable", requisition.errors["origin"]
        assertEquals "nullable", requisition.errors["destination"]
        assertEquals "nullable", requisition.errors["requestedBy"]
    }

}
