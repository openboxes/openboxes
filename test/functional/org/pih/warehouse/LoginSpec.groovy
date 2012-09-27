package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.LoginPage
import org.pih.warehouse.pages.LocationPage
import org.pih.warehouse.pages.DashboardPage

class LoginSpec extends  GebReportingSpec{

    def "should log in to the system if right user name and password is provided"(){
        given:
            to LoginPage
        when:
            loginForm.with{
                username = "manager"
                password = "password"
            }
        and:
            submitButton.click()
        and:
            at LocationPage
        and:
            boston.click()
        then:
            at DashboardPage

    }

}


