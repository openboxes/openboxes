package org.pih.warehouse.pages

import geb.Page
import testutils.Settings
import org.pih.warehouse.modules.DatePickerModule


class EnterShipmentDetailsPage extends Page{
    static url = Settings.baseUrl + "/createShipmentWorkflow/createShipment?type=OUTGOING"
    static at = { title == "Enter shipment details"}
    static content = {
        nextButton(to: EnterTrackingDetailsPage){$("button", name:"_eventId_next")}
        shipmentType { $("select", name:"shipmentType.id") }
        name{ $("input", name:"name")}
        origin{ $("select", name:"origin.id")}
        destination {$("select", name:"destination.id")}
        expectedShippingDate {$("input#expectedShippingDate-datepicker")}
        expectedArrivalDate {$("input#expectedDeliveryDate-datepicker")}
        datePicker{module DatePickerModule}
    }

}
