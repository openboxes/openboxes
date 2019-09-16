package org.pih.warehouse.pages

import geb.Page

import org.pih.warehouse.modules.DatePickerModule
import testutils.TestFixture

class SendShipmentPage extends  Page{
    static url = TestFixture.baseUrl + "/createShipmentWorkflow/createShipment"
    static at = { title == "Send Shipment"}
    static content = {
        nextButton(to: ViewShipmentPage){$("button", name:"_eventId_next")}
        actualShippingDate{$("input#actualShippingDate-datepicker")}
        datePicker{ module DatePickerModule}

    }
}
