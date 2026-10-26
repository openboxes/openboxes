package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest

import org.pih.warehouse.auth.AuthService

import spock.lang.Specification
import spock.lang.Unroll

/**
 * UserService.isUserInRoleAtLocation: a global role counts at every location, a location role only at its own
 * location, a higher role counts for a lower one, and the location the session is logged into plays no part.
 */
@Unroll
class UserServiceRoleAtLocationSpec extends Specification implements ServiceUnitTest<UserService>, DataTest {

    Location depotA
    Location depotB
    Map<RoleType, Role> roles = [:]

    void setupSpec() {
        mockDomain(Location)
        mockDomain(LocationRole)
        mockDomain(Role)
        mockDomain(User)
    }

    void setup() {
        [RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_ASSISTANT, RoleType.ROLE_BROWSER, RoleType.ROLE_PICKER].each {
            roles[it] = new Role(roleType: it, name: it.name).save(validate: false)
        }
        depotA = new Location(name: 'Depot A').save(validate: false)
        depotB = new Location(name: 'Depot B').save(validate: false)
        // the session is logged into depot A throughout: it must not matter
        service.authService = Stub(AuthService) {
            getCurrentLocation() >> depotA
        }
    }

    private User user(String username, List<RoleType> globalRoles, Map<Location, List<RoleType>> locationRoles = [:]) {
        User user = new User(username: username, password: 'password', passwordConfirm: 'password',
                firstName: username, lastName: 'User', email: "${username}@test.com")
        globalRoles.each { user.addToRoles(roles[it]) }
        user.save(validate: false)
        locationRoles.each { Location location, List<RoleType> roleTypes ->
            roleTypes.each { user.addToLocationRoles(new LocationRole(user: user, location: location, role: roles[it])) }
        }
        return user.save(validate: false)
    }

    void 'a global #held counts at every location for #asked: #expected'() {
        given:
        User u = user('global', [held])

        expect:
        service.isUserInRoleAtLocation(u, depotA, asked) == expected
        service.isUserInRoleAtLocation(u, depotB, asked) == expected

        where:
        held                    | asked                   || expected
        RoleType.ROLE_ASSISTANT | RoleType.ROLE_BROWSER   || true
        RoleType.ROLE_ASSISTANT | RoleType.ROLE_ASSISTANT || true
        RoleType.ROLE_ASSISTANT | RoleType.ROLE_MANAGER   || false
        RoleType.ROLE_ADMIN     | RoleType.ROLE_ASSISTANT || true
        RoleType.ROLE_BROWSER   | RoleType.ROLE_ASSISTANT || false
    }

    void 'a role held at depot B counts at depot B only, wherever the session is logged in'() {
        given:
        User u = user('located', [RoleType.ROLE_BROWSER], [(depotB): [RoleType.ROLE_ADMIN]])

        expect:
        service.isUserInRoleAtLocation(u, depotB, RoleType.ROLE_ADMIN)
        service.isUserInRoleAtLocation(u, depotB, RoleType.ROLE_ASSISTANT)
        !service.isUserInRoleAtLocation(u, depotA, RoleType.ROLE_ASSISTANT)
        service.isUserInRoleAtLocation(u, depotA, RoleType.ROLE_BROWSER)
    }

    void 'a supplementary role does not count as a primary one'() {
        given:
        User u = user('picker', [RoleType.ROLE_BROWSER, RoleType.ROLE_PICKER])

        expect:
        !service.isUserInRoleAtLocation(u, depotA, RoleType.ROLE_ASSISTANT)
    }

    void 'no user, no location or no role is never in role'() {
        given:
        User u = user('admin', [RoleType.ROLE_ADMIN])

        expect:
        !service.isUserInRoleAtLocation(null, depotA, RoleType.ROLE_BROWSER)
        !service.isUserInRoleAtLocation(u, null, RoleType.ROLE_BROWSER)
        !service.isUserInRoleAtLocation(u, depotA, null)
    }
}
