package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class EnterTrackingDetailsPage extends Page{
    static url = {Settings.BaseUrl + "/createShipmentWorkflow/createShipment"}
    static at = { title == "Enter tracking details"}
    static content = {
        nextButton(to: EnterTrackingDetailsPage){$("button", name:"_eventId_next")}
        containerNumber{$("input", name:"referenceNumbersInput.6")}
        comments{$("textarea", name:"additionalInformation")}
    }
}
