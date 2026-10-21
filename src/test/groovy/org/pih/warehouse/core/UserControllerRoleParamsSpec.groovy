package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.user.UserController

/**
 * UserController hands role parameters to UserService separately from the
 * fields it binds, so that the service can check them: a `roles` list becomes
 * the requested roles, and any other `roles...` or `locationRoles...` key is
 * removed before the user is bound or updated.
 */
class UserControllerRoleParamsSpec extends Specification implements ControllerUnitTest<UserController>, DataTest {

    UserService userService = Mock()

    Role superuserRole
    User requester
    User target
    Map call

    void setupSpec() {
        mockDomains(User, Role)
    }

    void setup() {
        controller.userService = userService
        superuserRole = new Role(roleType: RoleType.ROLE_SUPERUSER, name: 'Superuser').save(validate: false)
        controller.metaClass.warehouse = new Expando(message: { Map args -> 'message' })

        requester = new User(username: 'assistant', password: 'password', firstName: 'Assistant', lastName: 'User')
            .save(validate: false)
        target = new User(username: 'target', password: 'password', firstName: 'Target', lastName: 'User')
            .save(validate: false)
        session.user = requester
    }

    void "update passes a roles list to the service as the requested roles and strips other role keys"() {
        given:
        params.id = target.id
        params.firstName = 'Renamed'
        params.roles = ['role-a', 'role-b']
        params['roles[0].id'] = 'role-superuser'
        params['locationRoles[0].id'] = 'location-role'
        request.method = 'POST'

        when:
        controller.update()

        then:
        1 * userService.updateUser(*_) >> { String userId, String requestingUserId, List roles, Map passed ->
            call = [userId: userId, requestingUserId: requestingUserId, roles: roles, params: new HashMap(passed)]
            return target
        }
        call.userId == target.id
        call.requestingUserId == requester.id
        call.roles == ['role-a', 'role-b']
        call.params.firstName == 'Renamed'
        !call.params.keySet().any { it.toString().startsWith('roles') || it.toString().startsWith('locationRoles') }
        response.redirectedUrl == "/user/edit/${target.id}"
    }

    void "update with only a roles[0].id key requests no role change"() {
        given:
        params.id = target.id
        params['roles[0].id'] = 'role-superuser'
        request.method = 'POST'

        when:
        controller.update()

        then:
        1 * userService.updateUser(*_) >> { String userId, String requestingUserId, List roles, Map passed ->
            call = [roles: roles, params: new HashMap(passed)]
            return target
        }
        call.roles == []
        !call.params.keySet().any { it.toString().startsWith('roles') }
    }

    void "save binds no role to the new user and passes the roles list to the service"() {
        given:
        params.username = 'newuser'
        params.firstName = 'New'
        params.lastName = 'User'
        params.roles = [superuserRole.id]
        params['roles[0].id'] = superuserRole.id
        params['locationRoles[0].id'] = 'location-role'
        request.method = 'POST'

        when:
        controller.save()

        then:
        1 * userService.saveUser({ User user -> !user.roles && !user.locationRoles && user.username == 'newuser' },
            requester.id, [superuserRole.id]) >> { User user, String requestingUserId, List roles ->
            user.save(validate: false)
            return user
        }
    }
}
