package org.pih.warehouse.core

import geb.spock.GebSpec
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.pih.warehouse.Application
import org.pih.warehouse.page.DashboardPage
import org.pih.warehouse.page.LoginPage

@Integration(applicationClass = Application.class)
@Rollback
class UserFunctionalSpec extends GebSpec {

    def setup() {
        to LoginPage
        login("admin", "password")
        chooseLocation("Boston Headquarters")
        at DashboardPage
    }

    def cleanup() {
    }

    void "test page with user list loading"() {
        when:"The page with user list is visited"
            go '/user/list'

        then:"The title is correct"
            title == "Users"
    }
}
