package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class InventoryPage extends Page{
    static url = Settings.baseUrl + "/inventoryItem/showRecordInventory"
    static at = { title == "Record inventory"}
    static content ={
        productName { $("div.title").text().trim() }
        productCategory {$("#product-category").text().trim()}


    }
}