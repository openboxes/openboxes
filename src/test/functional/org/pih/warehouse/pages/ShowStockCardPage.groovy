package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture



class ShowStockCardPage extends  Page{
    static url = TestFixture.baseUrl + "/inventoryItem/showStockCard"
    static at = { waitFor{$("a#current-stock-tab")}.text() == "Current Stock"}
    static content ={
        currentStockTab{$("a#current-stock-tab")}
        actionButton{ $("button#product-action") }
        recordInventoryButton (wait:true, to: InventoryPage) { $("a[name='recordInventoryLink']") }
        totalQuantity{$("span#totalQuantity").text().trim()}
        stockProductName { $("div#product-title").text().trim() }
        productId{$("div#product-summary").@productid}
    }   
}
