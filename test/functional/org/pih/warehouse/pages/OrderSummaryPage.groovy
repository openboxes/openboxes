package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class OrderSummaryPage extends Page {
    static url = Settings.baseUrl + "/order/show/"
    static at = { title == "View order"}
    static content ={
        orderStatus { $("[name='status']") }
        placeOrderButton(to: OrderSummaryPage) { $("button", name:"placeOrder")}
    }
}
