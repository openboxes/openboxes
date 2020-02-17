package org.pih.warehouse.core

import geb.spock.GebSpec
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

import org.pih.warehouse.Application
import spock.lang.Ignore

@Integration(applicationClass = Application.class)
@Rollback
class UserFunctionalSpec extends GebSpec {

    def setup() {
    }

    def cleanup() {
    }

    @Ignore("TODO: fix java.lang.IllegalStateException: No GORM implementations configured. Ensure GORM has been initialized correctly at...")
    void "test page with user list loading"() {
        when:"The page with user list is visited"
            go '/user/list'

        then:"The title is correct"
        	title == "Users"
    }
}
