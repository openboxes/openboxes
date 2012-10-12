package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class AddOrderItemsPage extends Page {
    static url = Settings.baseUrl + "/purchaseOrderWorkflow/purchaseOrder"
    static at = { title == "Add order items"}
    static content ={
        inputProductName { $("input[name='product.name']") }
        inputQuantity { $("input[name='quantity']") }
        numItemInOrder { $("[name='numItemInOrder']") }
        addButton(to: AddOrderItemsPage) { $("input", type:"submit", name:"_eventId_addItem")}
        nextButton(to: OrderSummaryPage) { $("input", type:"submit", name:"_eventId_next")}
    }
}
