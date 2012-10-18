package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class ExpiringStockPage extends Page {
    static url = TestFixture.baseUrl + "/inventory/listExpiringStock"
    static at = { title == "Expiring stock"}
    static content = {
        category { $("select", name:"category") }
        threshhold { $("select", name:"threshhold") }
        filter { $("button", name:"filter") }
        expiringStockList { $("form#inventoryActionForm td.checkable")*.text() }
    }
}
