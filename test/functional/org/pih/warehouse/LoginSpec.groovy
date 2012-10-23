/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse

import geb.spock.GebReportingSpec
import org.pih.warehouse.pages.LoginPage
import org.pih.warehouse.pages.LocationPage
import org.pih.warehouse.pages.DashboardPage
import testutils.TestFixture

class LoginSpec extends  GebReportingSpec{

    def "should log in to the system if right user name and password is provided"(){
        given:
            to LoginPage
        when:
            userName.value("manager")
            password.value("password")
        and:
            submitButton.click()
        and:
            at LocationPage
        and:
            boston.click()
        then:
            at DashboardPage
            TestFixture.bostonManagerLogined()

    }

}


