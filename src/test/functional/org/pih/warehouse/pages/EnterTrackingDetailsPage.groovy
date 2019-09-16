package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class EnterTrackingDetailsPage extends Page{
    static url = TestFixture.baseUrl + "/createShipmentWorkflow/createShipment"
    static at = { title == "Enter tracking details"}
    static content = {
        nextButton(to: EditPackingListPage){$("button", name:"_eventId_next")}
        containerNumber{$("input", name:"referenceNumbersInput.6")}
        comments{$("textarea", name:"additionalInformation")}
    }
}
