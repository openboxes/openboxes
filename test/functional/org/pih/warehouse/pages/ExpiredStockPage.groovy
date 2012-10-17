package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class ExpiredStockPage extends Page {
    static url = Settings.baseUrl + "/inventory/listExpiredStock"
    static at = { title == "Expired stock"}
    static content = {
        expiredStockList { $("form#inventoryActionForm td.checkable")*.text() }
    }
}
