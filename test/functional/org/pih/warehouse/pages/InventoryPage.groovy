package org.pih.warehouse.pages

import geb.Page
import testutils.Settings
import org.pih.warehouse.modules.DatePickerModule

class InventoryPage extends Page{
    static url = Settings.baseUrl + "/inventoryItem/showRecordInventory"
    static at = { title == "Record inventory"}
    static content ={
        productName { $("div.title").text().trim() }
        productCategory {$("#product-category").text().trim()}
        unitOfMeasure {$("#unitOfMeasure").text().trim()}
        manufacturer {$("#manufacturer").text().trim()}
        manufacturerCode {$("#manufacturerCode").text().trim()}
        lotNumber{$("input", name:"recordInventoryRows[0].lotNumber")}
        expires{$("input", name:"recordInventoryRows[0].expirationDate-text")}
        newQuantity{$("input", name:"recordInventoryRows[0].newQuantity")}
        saveInventoryItem(to:ShowStockCardPage){$("button#saveInventoryItem")}
        datePicker{module DatePickerModule}
    }
}