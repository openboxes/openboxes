package unit.org.pih.warehouse.core

import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.PagedResultList
import org.pih.warehouse.PasswordCodec
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.user.UserController
import spock.lang.Specification

class UserControllerNewSpec extends Specification implements ControllerUnitTest<UserController> {

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

    void "test list action"() {

        given:
        List<User> users = [
                new User(username: "asd",
                        firstName: "Asd",
                        lastName: "Qwe",
                        password: "test123",
                        passwordConfirm: "test123")
        ]

        PagedResultList results = new PagedResultList(users)
        controller.userService = Stub(UserService.class)
        def listCriteria = [list: {Closure cls -> results}]
        User.metaClass.static.createCriteria = {listCriteria}
        controller.userService.findUsers(_ as String, _ as Map) >> results

        when: 'The list action is executed'
        controller.params.q = "asd"
        def model = controller.list()

        then: 'modal should have userInstanceList and userInstanceTotal'
        model.userInstanceList

    }

}
