package org.pih.warehouse.pages

import geb.Page
import testutils.Settings


class ShowStockCardPage extends  Page{
    static url = Settings.baseUrl + "/inventoryItem/showStockCard"
    static at = { $(".ui-tabs-nav a").first().text().contains("Current Stock")}
    static content ={
        actionButton { $("[name='actionButtonDropDown']") }
        recordInventoryButton (wait:true) { $("[name='recordInventoryLink']") }
        totalQuantity{$("span#totalQuantity").text().trim()}
        stockProductName { $("div.title").text().trim() }
    }

}
