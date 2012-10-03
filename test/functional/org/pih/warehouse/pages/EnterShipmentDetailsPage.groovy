package org.pih.warehouse.pages

import geb.Page
import testutils.Settings


class EnterShipmentDetailsPage extends Page{
    static url = {Settings.BaseUrl + "/createShipmentWorkflow/createShipment?type=OUTGOING"}
    static at = { title == "Enter shipment details"}
    static content = {
        nextButton(to: EnterTrackingDetailsPage){$("button", name:"_eventId_next")}
        shipmentType { $("select", name:"shipmentType.id") }
        name{ $("input", name:"name")}
        origin{ $("select", name:"ogirin.id")}
        destination {$("select", name:"destination.id")}
        expectedShippingDate {$("input", name:"expectedShippingDate")}
        expectedArrivalDate {$("input", name:"expectedDeliveryDate")}
    }

}
