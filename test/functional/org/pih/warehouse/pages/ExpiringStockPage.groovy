package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class ExpiringStockPage extends Page {
    static url = Settings.baseUrl + "/inventory/listExpiringStock"
    static at = { title == "Expiring stock"}
    static content = {
        threshhold { $("select", name:"threshhold") }
        filter { $("button", name:"filter") }
        expiredStockList { $("form#inventoryActionForm td.checkable")*.text() }
    }
}
