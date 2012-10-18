package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.AddOrderItemsPage
import org.pih.warehouse.pages.OrderSummaryPage
import org.pih.warehouse.pages.EnterOrderDetailsPage
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import testutils.TestFixture


class PurchaseOrderSpec extends GebReportingSpec {
    def "should create a new purchase order and add items"(){
        def productName =  "TestProd" + UUID.randomUUID().toString()[0..5]
        def orderDescription = "TestOrder" + UUID.randomUUID().toString()[0..5]
        given:
            def location = TestFixture.CreateSupplierIfRequired()
            TestFixture.UserLoginedAsManagerForBoston()
            TestFixture.CreateProductInInventory(productName, 50)
        and:
            to EnterOrderDetailsPage
        when:
            purchaseOrderDescription.value(orderDescription)
            purchaseOrderOrigin.value(location.id)
        and:
            addItemsButton.click()
        and:
            at AddOrderItemsPage

            inputProduct(productName)
            inputQuantity.value(10)
        and:
            addButton.click()
        and:
            at AddOrderItemsPage
            numItemInOrder.text() == "There are 1 items in this order."

        and:
            nextButton.click()
        then:
            at OrderSummaryPage
        and:
            placeOrderButton.click()
        then:
            at OrderSummaryPage
            orderStatus == "Placed"
            description == orderDescription
            productInfirstItem == productName
            quantityInfirstItem == "10"
    }
}
