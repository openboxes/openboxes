package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class BrowseInventoryPage extends Page {
    static url = TestFixture.baseUrl + "/inventory/browse"
    static at = { title == "Browse inventory"}
    static content ={
        selectProductCategory { $("select[name='subcategoryId']") }
        searchProductName (wait:true) { $("input[name='searchTerms']") }
        productItem (wait:true) { $("form[name='inventoryActionForm']").find("[name='productLink']").first() }
        searchButton(to: BrowseInventoryPage) { $("button", type:"submit", name:"searchPerformed")}
    }
}
