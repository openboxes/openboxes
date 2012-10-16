package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class BrowseInventoryPage extends Page {
    static url = Settings.baseUrl + "/inventory/browse"
    static at = { title == "Browse inventory"}
    static content ={
        selectProductCategory { $("select[name='subcategoryId']") }
        productItem { $("form[name='inventoryActionForm'] [name='productLink'] ") }
        searchButton(to: BrowseInventoryPage) { $("button", type:"submit", name:"searchPerformed")}
    }
}
