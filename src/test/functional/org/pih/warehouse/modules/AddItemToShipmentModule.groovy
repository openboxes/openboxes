package org.pih.warehouse.modules
import geb.Module

class AddItemToShipmentModule extends Module{
    static content={
        searchInventoryItem{module SearchInventoryItemModule}
        quantity(wait:true){$("input", name:"quantity")}
        saveButton(wait:true){$("form#editItemFound input#_eventId_saveItem")}
    }


}
