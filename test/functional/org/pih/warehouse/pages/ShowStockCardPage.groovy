package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture



class ShowStockCardPage extends  Page{
    static url = TestFixture.baseUrl + "/inventoryItem/showStockCard"
    static at = { $(".ui-tabs-nav a").first().text().contains("Current Stock")}
    static content ={
        actionButton { $("[name='actionButtonDropDown']") }
        recordInventoryButton (wait:true) { $("[name='recordInventoryLink']") }
        totalQuantity{$("span#totalQuantity").text().trim()}
        stockProductName { $("div.title").text().trim() }
    }

}
