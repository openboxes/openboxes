package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture

class ReceiveOrderConfirmPage extends Page {
    static url = TestFixture.baseUrl + "/receiveOrderWorkflow/receiveOrder"
    static at = { title == "Confirm order receipt"}
    static content = {
        receiveItemFinishButton { $("input", name:"_eventId_submit")}
    }
}
