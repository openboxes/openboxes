package org.pih.warehouse.pages

import geb.Page
import org.pih.warehouse.modules.AddItemToShipmentModule
import org.pih.warehouse.modules.AddSuitcaseToShipmentModule
import testutils.TestFixture

class EditPackingListPage extends Page{
    static url = TestFixture.baseUrl + "/createShipmentWorkflow/createShipment"
    static at = { title == "Add shipment items"}
    static content = {
        nextButton(to: SendShipmentPage){$("button", name:"_eventId_next")}
        actionButton{$("button.action-btn")}
        addItemToUnpackedItemsLink(wait:true){$("div#addItemToUnpackedItems a")}
        addPalletToShipmentLink(wait:true){$("div#addPalletToShipment a")}
        addCrateToShipmentLink(wait:true){$"div#addCrateToShipment a"}
        addSuitcaseToShipmentLink(wait:true){$("div#addSuitcaseToShipment a")}
        addItemToShipment{module AddItemToShipmentModule}
        addSuitcaseToShipment{module AddSuitcaseToShipmentModule}
    }

    def addItemToUnpackedItems(){
        actionButton.click()
        addItemToUnpackedItemsLink.click()
    }

    def addSuitcaseToShipment() {
        actionButton.click()
        addSuitcaseToShipmentLink.click()
    }
}
