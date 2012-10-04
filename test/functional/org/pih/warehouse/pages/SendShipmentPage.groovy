package org.pih.warehouse.pages

import geb.Page
import testutils.Settings
import org.pih.warehouse.modules.DatePickerModule

class SendShipmentPage extends  Page{
    static url = Settings.baseUrl + "/createShipmentWorkflow/createShipment"
    static at = { title == "Send Shipment"}
    static content = {
        nextButton(to: ViewShipmentPage){$("button", name:"_eventId_next")}
        actualShippingDate{$("input#actualShippingDate-datepicker")}
        datePicker{ module DatePickerModule}

    }
}
