package org.pih.warehouse.core

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.pih.warehouse.Application
import spock.lang.Ignore
import spock.lang.Specification

@Integration(applicationClass = Application.class)
@Rollback
class UserSpec extends Specification {

    def setup() {
        new User(username: "asd",
                firstName: "Asd",
                lastName: "Qwe",
                password: "test123",
                passwordConfirm: "test123").save(flush: true)
    }

    @Ignore("TODO: fix java.lang.IllegalStateException: No GORM implementations configured. Ensure GORM has been initialized correctly at...")
    void "test user count"() {
        expect:
        User.count() == 1
    }
}
