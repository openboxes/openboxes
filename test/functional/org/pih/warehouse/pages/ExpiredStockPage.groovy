package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class ExpiredStockPage extends Page {
    static url = TestFixture.baseUrl + "/inventory/listExpiredStock"
    static at = { title == "Expired stock"}
    static content = {
        expiredStockList { $("form#inventoryActionForm td.checkable")*.text() }
    }
}
