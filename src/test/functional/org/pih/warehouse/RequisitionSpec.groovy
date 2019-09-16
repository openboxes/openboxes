package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.CreateRequisitionPage
import org.pih.warehouse.pages.EditRequisitionPage
import org.pih.warehouse.pages.ProcessRequisitionPage
import org.pih.warehouse.pages.ShowRequisitionPage
import testutils.TestFixture

class RequisitionSpec extends GebReportingSpec {

    def "create a new requisition and add requisition items to it"() {
        def productName =  "TestProd" + UUID.randomUUID().toString()[0..5]
        def productName2 =  "TestProd" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(productName, 5000)
            TestFixture.CreateProductInInventory(productName2, 6000)
        and:
            to CreateRequisitionPage
        when:
            selectRequestingDepot.value(2) // Miama Depot
            autocompleteRequestedBy.value("Justin")
            firstSuggestion.click()
        and:
            save()
            report "requistion created"
        then:
            at EditRequisitionPage
            firstRequisitionItemProduct.value(productName)
            firstProductSuggestion.click()
            firstRequisitionItemQuantity.value("2000")
            addRowButton.click()
            secondRequisitionItemProduct.value(productName2)
            secondProductSuggestion.click()
            secondRequisitionItemQuantity.value("4000")
        and:
            submitRequisitionButton.click()
        then:
            at ShowRequisitionPage
            firstProductName == productName
            firstProductQuantity == "2000"
            firstProductQuantityPicked == "0"
            secondProductName == productName2
            secondProductQuantity == "4000"
            secondProductQuantityPicked == "0"
        and:
            requisitionActionButton.click()
            processRequisitionActionButton.click()
        and:
            at ProcessRequisitionPage
            firstAccordionRow.click()
            firstRowContentPicked.value("1000")
        when:
            processRequisitionButton.click()
            Thread.sleep(3000)
        then:
            at ShowRequisitionPage
            firstProductQuantityPicked == "1000"
            secondProductQuantityPicked == "0"
    }
}
