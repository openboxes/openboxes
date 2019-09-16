package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class ReceiveOrderItemsPage extends Page {
    static url = TestFixture.baseUrl + "/receiveOrderWorkflow/receiveOrder"
    static at = { title == "Add order items"}
    static content = {
        receiveItemQuantity (wait:true) { $("input", name:"orderItems[0].quantityReceived") }
        receiveItemLotNumber (wait:true) { $("input", name:"orderItems[0].lotNumber") }
        receiveItemNextButton { $("input", name:"_eventId_next") }
    }
}
