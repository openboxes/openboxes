package org.pih.warehouse.pages

import geb.Page

import org.pih.warehouse.modules.DatePickerModule
import testutils.TestFixture


class CreateEnterShipmentDetailsPage extends Page{
    static url = TestFixture.baseUrl + "/createShipmentWorkflow/createShipment?type=OUTGOING"
    static at = { title == "Enter shipment details"}
    static content = {
        nextButton(to: EnterTrackingDetailsPage){$("button", name:"_eventId_next")}
        shipmentType { $("select", name:"shipmentType.id") }
        shipmentName{ $("input", name:"name")}
        origin{ $("select", name:"origin.id")}
        destination {$("select", name:"destination.id")}
        expectedShippingDate {$("input#expectedShippingDate-datepicker")}
        expectedArrivalDate {$("input#expectedDeliveryDate-datepicker")}
        datePicker{module DatePickerModule}
    }

}
