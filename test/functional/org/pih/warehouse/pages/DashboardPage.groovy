package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class DashboardPage extends Page{
    static url = { Settings.baseUrl + "dashboard/index"}
    static at = { title == "Dashboard"}
    static content = {

    }
}
