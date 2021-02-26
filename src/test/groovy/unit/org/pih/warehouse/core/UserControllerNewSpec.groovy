package unit.org.pih.warehouse.core

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.testing.GrailsUnitTest
import org.pih.warehouse.PasswordCodec
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.user.UserController
import spock.lang.Specification

//@Mock(User)
@TestFor(UserController)
@Mock([User, UserService])
class UserControllerNewSpec extends Specification {
    def stubMessager = new Expando()

    def setup() {
        mockCodec(PasswordCodec)
    }

    def cleanup() {
    }

    void "test index action"() {

        when: 'The index action is executed'
        controller.index()

        then: 'The response redirect should /user/list'
        response.redirectedUrl == "/user/list"

    }

// Failing with "No such property: totalCount for class: org.pih.warehouse.core.User"
    void "test list action"() {

        given:
        List<User> users = [
                new User(username: "asd",
                        firstName: "Asd",
                        lastName: "Qwe",
                        password: "test123",
                        passwordConfirm: "test123")
        ]
        controller.userService = Stub(UserService.class)
        def listCriteria = [list: {Closure cls -> users}]
        Person.metaClass.static.createCriteria = {listCriteria}
        controller.userService.findUsers(_ as String, _ as Map) >> users

        when: 'The list action is executed'
        controller.params.q = "asd"
        controller.list()

        then: 'modal should have userInstanceList and userInstanceTotal'
        model.userInstanceList

    }

}