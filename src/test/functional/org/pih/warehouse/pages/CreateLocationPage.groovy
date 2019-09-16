package org.pih.warehouse.pages

import testutils.TestFixture
import geb.Page

class CreateLocationPage extends  Page{
    static url = TestFixture.baseUrl + "/location/edit"
    static at = { title == "Edit Warehouse"}
    static content ={
        locationName{ $("input#name") }
        locationType{ $("select", name: "locationType.id")}
        supportActivities{ $("select#supportedActivities")}
        saveButton{ $("button",type:"submit")}
    }
}
