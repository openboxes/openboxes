package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.AddOrderItemsPage
import org.pih.warehouse.pages.OrderSummaryPage
import org.pih.warehouse.pages.EnterOrderDetailsPage
import testutils.DbHelper
import testutils.PageNavigator

class PurchaseOrderSpec extends GebReportingSpec {
    def "should create a new purchase order and add items"(){
        given:
            def location = DbHelper.CreateSupplierIfRequired()
            PageNavigator.UserLoginedAsManagerForBoston()
        and:
            to EnterOrderDetailsPage
        when:
            purchaseOrderDescription.value("TestingPurchaseOrder")
            purchaseOrderOrigin.value(location.id)
        and:
            addItemsButton.click()
        and:
            at AddOrderItemsPage
            def product = DbHelper.CreateProductInInventory("Tylenol", 50)
            inputProductName.value(product.product.name)
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
            orderStatus.text() == "Placed"
    }
}
