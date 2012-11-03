package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class BrowseInventoryPage extends Page {
    static url = TestFixture.baseUrl + "/inventory/browse"
    static at = { title == "Browse inventory"}
    static content ={
        selectProductCategory { $("select[name='subcategoryId']") }
        productItem (to: ShowStockCardPage) { $("tr.product a[name='productLink']")}

        searchButton(to: BrowseInventoryPage) { $("button", type:"submit", name:"searchPerformed")}
        searchText{$("input#searchTerms")}
    }

}
