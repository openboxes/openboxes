package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class SendShipmentPage extends  Page{
      static url = {Settings.BaseUrl + "/createShipmentWorkflow/createShipment"}
    static at = { title == "find the right title"}
    static content = {
        //saveButton(to: SendShipmentPage){$("button", name:"_eventId_save")}

    }
}
