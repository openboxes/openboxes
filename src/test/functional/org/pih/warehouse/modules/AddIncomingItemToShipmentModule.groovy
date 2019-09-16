package org.pih.warehouse.modules

import geb.Module

class AddIncomingItemToShipmentModule extends Module {
    static content = {
        searchIncomingInventoryItem{module SearchIncomingInventoryItemModule}
        quantity(wait:true){$("input#quantity")}
        saveButton(wait:true){$("form#addIncomingItem input#_eventId_saveItem")}
    }
}