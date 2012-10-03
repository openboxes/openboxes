package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class SendShipmentPage extends  Page{
    static url = {Settings.BaseUrl + "/createShipmentWorkflow/createShipment"}
    static at = { title == "Send Shipment"}
    static content = {
        nextButton(to: ViewShipmentPage){$("button", name:"_eventId_next")}
        actualShippingDate{$("input",name:"actualShippingDate")}


    }
}
