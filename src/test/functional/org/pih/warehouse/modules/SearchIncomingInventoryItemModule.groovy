package org.pih.warehouse.modules

import geb.Module

class SearchIncomingInventoryItemModule extends Module {
    static content = {
        searchCriteria(wait: true) {$("input#productSearch")}
        firstSuggestion(wait: true) {$("table#results button.choose").first()}
    }

    def findProduct(product_name) {
        searchCriteria.value(product_name)
        waitFor{ $("#results td.label")}.first().text() == product_name
        firstSuggestion.click()
    }
}