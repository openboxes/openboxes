package org.pih.warehouse.pages

import geb.Page


class ShowStockCardPage extends  Page{
    static url = {settings.baseUrl + "/inventoryItem/showStockCard"}
    static at = { $(".ui-tabs-nav a").text().contains("Current Stock")}
    static content ={
        totalQuantity{$("span#totalQuantity").text().trim()}
        productName { $("div.title").text().trim() }
    }

}
