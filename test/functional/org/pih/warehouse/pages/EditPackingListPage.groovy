package org.pih.warehouse.pages

import geb.Page
import org.pih.warehouse.modules.AddItemToShipmentModule
import org.pih.warehouse.modules.AddIncomingItemToShipmentModule
import org.pih.warehouse.modules.AddSuitcaseToShipmentModule
import testutils.TestFixture

class EditPackingListPage extends Page{
    static url = TestFixture.baseUrl + "/createShipmentWorkflow/createShipment"
    static at = { title == "Add shipment items"}
    static content = {
        nextButton(to: SendShipmentPage){$("button", name:"_eventId_next")}
        saveAndExitButton(to:ViewShipmentPage){$("button", name:"_eventId_save")}
        actionButton{$("button.action-btn")}
        addItemToUnpackedItemsLink(wait:true){$("div#addItemToUnpackedItems a")}
        addPalletToShipmentLink(wait:true){$("div#addPalletToShipment a")}
        addCrateToShipmentLink(wait:true){$"div#addCrateToShipment a"}
        addSuitcaseToShipmentLink(wait:true){$("div#addSuitcaseToShipment a")}
        addItemToShipment{module AddItemToShipmentModule}
        addIncomingItemToShipment{module AddIncomingItemToShipmentModule}
        addSuitcaseToShipment{module AddSuitcaseToShipmentModule}
    }

    def addItem(product_name, quantity){
        addItemToShipment.searchInventoryItem.searchCriteral.value(product_name)
        waitFor{$("ul.ui-autocomplete li.ui-menu-item a")}.first().text().contains(product_name) == true
        addItemToShipment.searchInventoryItem.firstSuggestion.click()
        addItemToShipment.quantity.value(quantity)
        addItemToShipment.saveButton.click()
    }

    def addIncomingItem(product_name, quantity){
        addIncomingItemToShipment.searchIncomingInventoryItem.findProduct(product_name)
        addIncomingItemToShipment.quantity.value(quantity)
        addIncomingItemToShipment.saveButton.click()
    }

    def addUnpackedItems(){
        actionButton.click()
        addItemToUnpackedItemsLink.click()
    }

    def addSuitcase(specs) {
        actionButton.click()
        addSuitcaseToShipmentLink.click()
        addSuitcaseToShipment.packingUnit.value(specs.unit)
        addSuitcaseToShipment.weight.value(specs.weight)
        addSuitcaseToShipment.caseHeight.value(specs.height)
        addSuitcaseToShipment.caseWidth.value(specs.width)
        addSuitcaseToShipment.caseLength.value(specs.length)
        addSuitcaseToShipment.addItemButton.click()
    }


}
