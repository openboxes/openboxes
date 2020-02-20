package org.pih.warehouse.core

import grails.test.mixin.Mock
import spock.lang.Ignore
import spock.lang.Specification

@Mock(User)
class UserDomainSpec extends Specification {

    void "test validation on user with all required and valid data"() {
        when:
        def user = new User(
                username: "asd",
                firstName: "Asd",
                lastName: "Qwe",
                password: "test123")

        then:
        user.validate()
    }

    void "test validation on user with missing required data"() {
        when:
        def user = new User(
                firstName: "Asd",
                lastName: "Qwe",
                password: "test123")

        then:
        !user.validate()
    }

    @Ignore("somehow passwordConfirm is null")
    void "test validation on user with invalid data"() {
        when:
        def user = new User(
                username: "asd",
                firstName: "Asd",
                lastName: "Qwe",
                password: "PASSWORD123",
                passwordConfirm: "WRONG_CONFIRM_PASSWORD")

        then:
        !user.validate()
    }
}
