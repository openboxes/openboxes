package org.pih.warehouse.core

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.pih.warehouse.Application
import spock.lang.Specification

@Integration(applicationClass = Application.class)
@Rollback
class UserSpec extends Specification {

    void setupData() {
        new User(username: "asd",
                firstName: "Asd",
                lastName: "Qwe",
                password: "test123",
                passwordConfirm: "test123").save(flush: true)
    }

    void "test user count"() {
        given:
        setupData()

        expect:
        User.count() == 5 //there are some initial users added in the migration
    }
}
