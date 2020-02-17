package org.pih.warehouse.core

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.pih.warehouse.PasswordCodec
import org.pih.warehouse.user.UserController
import spock.lang.Specification

@TestFor(UserController)
@Mock([User, UserService])
class UserControllerSpec extends Specification {
    def stubMessager = new Expando()

    void setup() {
        mockCodec(PasswordCodec)

        new User(username: "asd",
            firstName: "Asd",
            lastName: "Qwe",
            password: "test123",
            passwordConfirm: "test123").save(flush: true)
    }

    void "test redirecting from index action"() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/user/list'
    }
    void "test redirect action"() {
        when:
        controller.redirect()

        then:
        response.redirectedUrl == '/user/edit'
    }

    void "test create user"() {
        when:
        controller.params.username = "Test"
        controller.params.password = "Password123"
        def model = controller.create()

        then:
        model.userInstance.username == "Test"
        model.userInstance.password == "Password123"
    }

    void "test saving an invalid user"() {
        when:
        request.method = "POST"
        controller.save()

        then:
        view == '/user/create'
        model.userInstance.username == null
        model.userInstance.password == null
    }

    void "test saving a valid user"() {
        when:
        stubMessager.message = { args -> return "success" }
        controller.metaClass.warehouse = stubMessager
        controller.params.username = "Test"
        controller.params.firstName = "John"
        controller.params.lastName = "Doe"
        controller.params.password = "Password123"
        controller.params.passwordConfirm = "Password123"
        request.method = "POST"
        controller.save()

        then:
        response.redirectedUrl.startsWith('/user/edit/')
        flash.message != null
        User.count() == 2
    }

    void "test get list of users with wrong query param"() {
        when:
        controller.params.q = "ZZZZZZ"
        def listOfUsers = controller.list()

        then:
        listOfUsers.userInstanceList.size() == 0
        listOfUsers.userInstanceTotal == 0
    }

    void "test get list of users with proper query param"() {
        when:
        controller.params.q = "Asd"
        def listOfUsers = controller.list()

        then:
        listOfUsers.userInstanceList.size() == 1
        listOfUsers.userInstanceTotal == 1
    }
}
