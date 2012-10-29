package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase


class RequisitionTests extends GrailsUnitTestCase {

    void testNotNullableConstraints() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition()
        assertFalse requisition.validate()
        assertEquals "nullable", requisition.errors["origin"]
        assertEquals "nullable", requisition.errors["destination"]
        assertEquals "nullable", requisition.errors["requestedBy"]
        assertEquals "nullable", requisition.errors["dateRequested"]
        assertEquals "nullable", requisition.errors["requestedDeliveryDate"]
    }

    void testDateRequestedCannotBeGreaterThanToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date().plus(1))
        assertFalse requisition.validate()
        assert requisition.errors["dateRequested"]
    }

     void testDateRequestedCanbeToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date())
        requisition.validate()
        assertNull requisition.errors["dateRequested"]
    }

     void testDateRequestedCanbeLessThanToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(dateRequested:new Date().minus(6))
        requisition.validate()
        assertNull requisition.errors["dateRequested"]
    }

    void testRequestedDeliveryDateGreaterThanToday() {
        mockForConstraintsTests(Requisition)
        def tomorrow = new Date().plus(1)
        tomorrow.clearTime()
        def requisition = new Requisition(requestedDeliveryDate: tomorrow)
        requisition.validate()
        assertNull requisition.errors["requestedDeliveryDate"]
    }

    void testRequestedDeliveryDateCannotBeToday() {
        mockForConstraintsTests(Requisition)
        def requisition = new Requisition(requestedDeliveryDate:new Date())
        requisition.validate()
        assert requisition.errors["requestedDeliveryDate"]
    }

}
