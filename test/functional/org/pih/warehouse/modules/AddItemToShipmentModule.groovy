package org.pih.warehouse.modules
import geb.Module

class AddItemToShipmentModule extends Module{
    static content={
        searchInventoryItem{module SearchInventoryItemModule}
        quantity{$("input", name:"quantity")}
        saveButton{$("form#editItemFound input#_eventId_saveItem")}
    }
}
