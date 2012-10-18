package org.pih.warehouse.pages

import geb.Page

import testutils.TestFixture

class AddOrderItemsPage extends Page {
    static url = TestFixture.baseUrl + "/purchaseOrderWorkflow/purchaseOrder"
    static at = { title == "Add order items"}
    static content ={

        inputQuantity { $("input[name='quantity']") }
        numItemInOrder { $("[name='numItemInOrder']") }
        addButton(to: AddOrderItemsPage) { $("input", type:"submit", name:"_eventId_addItem")}
        nextButton(to: OrderSummaryPage) { $("input", type:"submit", name:"_eventId_next")}
        searchProductCriteria {$("input#product-suggest")}
        firstSuggestion(wait: true){$("ul.ui-autocomplete li.ui-menu-item a").first()}
    }

    def inputProduct(productName){
        searchProductCriteria.value(productName)
        firstSuggestion.click()
    }
}
