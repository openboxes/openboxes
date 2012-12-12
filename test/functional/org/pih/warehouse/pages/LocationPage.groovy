
package org.pih.warehouse.pages

import geb.Page
import testutils.TestFixture


class LocationPage extends  Page{
    static url = TestFixture.baseUrl + "/dashboard/chooseLocation"
    static at = { title == "Choose a location"}
    static content ={
        boston(wait:true, to: DashboardPage) { $("#warehouse-1-link")}
        miami(wait:true, to: DashboardPage) { $("#warehouse-2-link")}
        tabarre(wait:true, to: DashboardPage) { $("#warehouse-3-link")}
    }
    def chooseLocation(String location){
      if(location == "boston")
        boston.click()
      else if(location == "miami")
        miami.click()
      else if(location == "tabarre")
        tabarre.click()      
    }
}
