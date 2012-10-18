package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class OrderSummaryPage extends Page {
    static url = TestFixture.baseUrl + "/order/show/"
    static at = { title == "View order"}
    static content ={
        orderStatus { $("[name='status']").text() }
        placeOrderButton(to: OrderSummaryPage) { $("button", name:"placeOrder")}
        description { $("#order-description").text()}
        origin { $("#order-origin").text()}
        destination { $("#order-destination").text()}
        productInfirstItem { $("tr.order-item").first().find("td.order-item-product").text()}
        quantityInfirstItem { $("tr.order-item").first().find("td.order-item-quantity").text()}
    }
}
