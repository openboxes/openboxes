package org.pih.warehouse.pages

import geb.Page
import org.pih.warehouse.modules.DatePickerModule
import testutils.TestFixture

class ReceiveShipmentPage extends Page {
    static url = TestFixture.baseUrl + "/shipment/receiveShipment"
    static at = { title == "Receive shipment"}
    static content = {
        status {$("span.status")}
        deliveredOnDate {$("input#actualDeliveryDate-datepicker")}
        datePicker{ module DatePickerModule}
        saveButton(to: ViewShipmentPage) {$("div.buttons button.positive")}
    }
}