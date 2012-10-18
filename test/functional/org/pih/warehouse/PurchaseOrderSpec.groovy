package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.AddOrderItemsPage
import org.pih.warehouse.pages.OrderSummaryPage
import org.pih.warehouse.pages.EnterOrderDetailsPage
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import testutils.TestFixture
import testutils.PageNavigator

class PurchaseOrderSpec extends GebReportingSpec {
    def "should create a new purchase order and add items"(){
        def productName =  "TestProd" + UUID.randomUUID().toString()[0..5]
        def orderDescription = "TestOrder" + UUID.randomUUID().toString()[0..5]
        given:
            def location = TestFixture.CreateSupplierIfRequired()
            PageNavigator.UserLoginedAsManagerForBoston()
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
        //todo: verify by UI
//            def po_id = Order.executeQuery("select id from Order o order by o.dateCreated")[0]
//            def po_item = OrderItem.executeQuery("select product.id from OrderItem oi where oi.order.id = ?", po_id)[0]
//            po_item == product.id
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
