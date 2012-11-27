package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.CreateRequisitionPage
import org.pih.warehouse.pages.EditRequisitionPage
import testutils.TestFixture

class RequisitionSpec extends GebReportingSpec {

    def "create a new requisition and add 2 requisition items to it"() {
        def productName =  "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            def location = TestFixture.CreateSupplierIfRequired()
            TestFixture.CreateProductInInventory(productName, 5000)
        and:
            to CreateRequisitionPage
        and:
            selectRequestingDepot.value(2) // Miama Depot
            autocompleteRequestedBy.value("Justin")
            firstSuggestion.click()
        and:
            createRequisitionButton.click()
        expect:
            at EditRequisitionPage

    }
}
