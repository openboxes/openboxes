package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class EditPackingListPage extends Page{
    static url = {Settings.BaseUrl + "/createShipmentWorkflow/createShipment"}
    static at = { title == "Add shipment items"}
    static content = {
        nextButton(to: SendShipmentPage){$("button", name:"_eventId_next")}
        actionButton{$("button.action-btn")}
        addItemToUnpackedItemsLink{$("div.addItemToUnpackedItems a")}
        addPalletToShipmentLink{$("div.addPalletToShipment a")}
        addCrateToShipmentLink{$"div.addCrateToShipment a"}

    }
}
