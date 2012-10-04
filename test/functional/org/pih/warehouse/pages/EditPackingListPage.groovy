package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

import org.pih.warehouse.modules.AddItemToShipmentModule

class EditPackingListPage extends Page{
    static url = Settings.baseUrl + "/createShipmentWorkflow/createShipment"
    static at = { title == "Add shipment items"}
    static content = {
        nextButton(to: SendShipmentPage){$("button", name:"_eventId_next")}
        actionButton{$("button.action-btn")}
        addItemToUnpackedItemsLink(wait:true){$("div#addItemToUnpackedItems a")}
        addPalletToShipmentLink(wait:true){$("div#addPalletToShipment a")}
        addCrateToShipmentLink(wait:true){$"div#addCrateToShipment a"}
        addItemToShipment{module AddItemToShipmentModule}
    }

    def addItemToUnpackedItems(){
        actionButton.click()
        addItemToUnpackedItemsLink.click()
    }


}
