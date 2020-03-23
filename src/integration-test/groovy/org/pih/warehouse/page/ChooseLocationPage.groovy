package org.pih.warehouse.page

import geb.Page

class ChooseLocationPage extends Page {
    static url = "/dashboard/chooseLocation"

    static at = {
        title == 'Choose Location'
    }

    static content = {
        locationButton(to: DashboardPage) { locationName -> $('a', 0, text: locationName) }
    }

    void chooseLocation(String locationName) {
        locationButton(locationName).click()
    }
}
