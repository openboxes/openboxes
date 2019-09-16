package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class OrderSummaryPage extends Page {
    static url = TestFixture.baseUrl + "/order/show/"
    static at = { title == "View order"}
    static content ={
        orderStatus { $("[name='status']").text() }
        placeOrderButton(to: OrderSummaryPage) { $("button", name:"placeOrder")}
        orderActionButton { $("button", class:"action-btn") }
        receiverOrderActionButton { $("[name='receiveOrderLink']") }
        name { $("#order-name").text()}
        origin { $("#order-origin").text()}
        destination { $("#order-destination").text()}
        productInfirstItem { $("tr.order-item").first().find("td.order-item-product").text()}
        quantityInfirstItem { $("tr.order-item").first().find("td.order-item-quantity").text()}
    }
}
