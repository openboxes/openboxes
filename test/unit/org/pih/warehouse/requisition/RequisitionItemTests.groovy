package org.pih.warehouse.requisition

import grails.test.GrailsUnitTestCase

class RequisitionItemTests extends GrailsUnitTestCase {

    void testNotNullableConstraints() {
        mockForConstraintsTests(RequisitionItem)
        def requisitionItem = new RequisitionItem()
        assertFalse requisitionItem.validate()
        assertEquals "nullable", requisitionItem.errors["product"]
        assertEquals "nullable", requisitionItem.errors["quantity"]
    }


}
