package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.AddOrderItemsPage
import org.pih.warehouse.pages.OrderSummaryPage
import org.pih.warehouse.pages.EnterOrderDetailsPage
import org.pih.warehouse.pages.ReceiveEnterShipmentDetailsPage
import org.pih.warehouse.pages.ReceiveOrderConfirmPage
import org.pih.warehouse.pages.ReceiveOrderItemsPage
import org.pih.warehouse.pages.ShowStockCardPage
import testutils.TestFixture


class PurchaseOrderSpec extends GebReportingSpec {
    def "should create a new purchase order and add items then receive the order and verify its status has changed"(){
        def productName =  "TestProd" + UUID.randomUUID().toString()[0..5]
        def productId
        def orderDescription = "TestOrder" + UUID.randomUUID().toString()[0..5]
        given:
            TestFixture.UserLoginedAsManagerForBoston()
            def location = TestFixture.GetSupplierLocation()
            productId = TestFixture.CreateProductInInventory(productName, 50)
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
            report "order created"
        and:
            placeOrderButton.click()
        then:
            at OrderSummaryPage
            orderStatus == "Placed"
            description == orderDescription
            productInfirstItem == productName
            quantityInfirstItem == "10"
        and:
            orderActionButton.click()
            receiverOrderActionButton.click()
        and:
            at ReceiveEnterShipmentDetailsPage
            shipmentType.value("2") //Sea shipment
            shippedOnDate.click()
            datePicker.pickDate(new Date())
            deliveredOnDate.click()
            datePicker.pickDate(new Date())
            receiveOrderNextButton.click()
        and:
            at ReceiveOrderItemsPage
            receiveItemQuantity.value(5)
            receiveItemLotNumber.value("ABCD")
            receiveItemNextButton.click()
        and:
            at ReceiveOrderConfirmPage
            receiveItemFinishButton.click()
        then:
            at OrderSummaryPage
            orderStatus == "Partially Received"
            description == orderDescription
            productInfirstItem == productName
            quantityInfirstItem == "10"
        and:
            orderActionButton.click()
            receiverOrderActionButton.click()
        and:
            at ReceiveEnterShipmentDetailsPage
            shipmentType.value("2") //Sea shipment
            shippedOnDate.click()
            datePicker.pickDate(new Date())
            deliveredOnDate.click()
            datePicker.pickDate(new Date())
            receiveOrderNextButton.click()
        and:
            at ReceiveOrderItemsPage
            receiveItemQuantity.value(5)
            receiveItemLotNumber.value("EFDG")
            receiveItemNextButton.click()
        and:
            at ReceiveOrderConfirmPage
            receiveItemFinishButton.click()
        then:
            at OrderSummaryPage
            orderStatus == "Received"
            description == orderDescription
            productInfirstItem == productName
            quantityInfirstItem == "10"
        and:
            TestFixture.verifyInventoryQuantityForProduct(productId, 60)           
    }

}
