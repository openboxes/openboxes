
package org.pih.warehouse.pages

import geb.Page
import testutils.Settings

class LocationPage extends  Page{
    static url = {Settings.baseUrl + "/dashboard/chooseLocation"  }
    static at = { title == "Choose a location"}
    static content ={
        boston(to: DashboardPage) { $("div#warehouse-1 a")}
        miami(to: DashboardPage) { $("div#warehouse-2 a")}
        tabarre(to: DashboardPage) { $("div#warehouse-3 a")}
    }
}
