package org.pih.warehouse.pages

import geb.Page
import testutils.Settings


class ShowStockCardPage extends  Page{
    static url = Settings.baseUrl + "/inventoryItem/showStockCard"
    static at = { $(".ui-tabs-nav a").first().text().contains("Current Stock")}
    static content ={
        totalQuantity{$("span#totalQuantity").text().trim()}
        productName { $("div.title").text().trim() }
    }

}
