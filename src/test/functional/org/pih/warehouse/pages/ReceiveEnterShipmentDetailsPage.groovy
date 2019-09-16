package org.pih.warehouse.pages

import geb.Page
import org.pih.warehouse.modules.DatePickerModule
import testutils.TestFixture

class ReceiveEnterShipmentDetailsPage extends Page{
    static url = TestFixture.baseUrl + "/receiveOrderWorkflow/receiveOrder"
    static at = { title == "Enter shipment details"}
    static content = {
        shipmentType { $("select", name:"shipmentType.id") }
        shippedOnDate {$("input#shippedOn-datepicker")}
        deliveredOnDate {$("input#deliveredOn-datepicker")}
        datePicker{module DatePickerModule}
        receiveOrderNextButton { $("input", name:"_eventId_next") }
    }

}
