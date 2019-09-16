package org.pih.warehouse.pages

import geb.Page
import org.pih.warehouse.modules.AddItemToShipmentModule
import org.pih.warehouse.modules.AddIncomingItemToShipmentModule
import org.pih.warehouse.modules.AddContainerToShipmentModule
import testutils.TestFixture
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;


class EditPackingListPage extends Page{
	
    static url = TestFixture.baseUrl + "/createShipmentWorkflow/createShipment"
    static at = { title == "Add shipment items"}
    static content = {
        nextButton(to: SendShipmentPage){$("button", name:"_eventId_next")}
        saveAndExitButton(to:ViewShipmentPage){$("button", name:"_eventId_save")}
        addItemToUnpackedItemsLink(wait:true){$("div#addItemToUnpackedItems a")}
        addPalletToShipmentLink(wait:true){$("div#addPalletToShipment a")}
        addCrateToShipmentLink(wait:true){$"div#addCrateToShipment a"}
        addSuitcaseToShipmentLink(wait:true){$("div#addSuitcaseToShipment a")}
        addItemToShipment{module AddItemToShipmentModule}
        addIncomingItemToShipment{module AddIncomingItemToShipmentModule}
        addContainerToShipment{module AddContainerToShipmentModule}
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
        clickPackingAction()
        addItemToUnpackedItemsLink.click()
    }

    def addPallet(pallet){
        clickPackingAction()
        addPalletToShipmentLink.click()
        addContainerToShipment.addContainer(pallet)
        addContainerToShipment.addItemButton.click()

    }

    def addCrate(crate){
        clickPackingAction()
        addCrateToShipmentLink.click()
        addContainerToShipment.addContainer(crate)
        addContainerToShipment.addItemButton.click()

    }

    def addSuitcase(suitcase) {
        clickPackingAction()
        addSuitcaseToShipmentLink.click()
        addContainerToShipment.addContainer(suitcase)
        addContainerToShipment.addItemButton.click()
    }

    def clickPackingAction(){
        waitFor{$("button.action-btn")}.click() //do not put into content because it will show/hide multiple times		
		//waitFor{$("#unpackedItemsActionBtn")}.click()
    }


}
