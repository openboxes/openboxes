package org.pih.warehouse.api.regression.platform.core


import spock.lang.Ignore
import org.pih.warehouse.core.*
import grails.core.GrailsApplication
import grails.validation.ValidationException

import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
/**
 * The user and person searches, role lookups, default roles and person-to-user conversion against a real database.
 */
class UserServiceIntegrationSpec extends ServiceIntegrationSpec {

    UserService userService
    GrailsApplication grailsApplication

    Location depot
    String marker
    User browser
    User manager

    void setup() {
        depot = newDepot('User depot')
        marker = uniqueName('usr').replaceAll('[^A-Za-z0-9]', '')
        browser = newUser("browser${marker}", [RoleType.ROLE_BROWSER], [:])
        manager = newUser("manager${marker}", [], [(depot): RoleType.ROLE_MANAGER])
    }

    private User newUser(String username, List<RoleType> roles, Map<Location, RoleType> locationRoles) {
        return committed {
            User user = new User(username: username, password: 'password', passwordConfirm: 'password', firstName: username,
                    lastName: "Last${marker}", email: "${username}@example.com", active: true)
            roles.each { user.addToRoles(Role.findByRoleType(it)) }
            locationRoles.each { Location location, RoleType roleType ->
                user.addToLocationRoles(new LocationRole(location: Location.get(location.id), role: Role.findByRoleType(roleType)))
            }
            user.save(failOnError: true, flush: true)
        }
    }

    void 'users and people are searched by name and role'() {
        when:
        Map result = asAdminAt(depot) {
            [
                    byTerm        : userService.findUsers([searchTerm: "Last${marker}", active: 'true'])*.id as Set,
                    byRole        : userService.findUsers([searchTerm: "Last${marker}", roleTypes: ['ROLE_BROWSER']])*.id,
                    byLocationRole: userService.findUsers([searchTerm: "Last${marker}", roleTypes: ['ROLE_MANAGER'], location: depot.id])*.id,
                    persons       : userService.findPersons(["Last${marker}"] as String[], [status: 'true', sort: 'firstName', order: 'asc'])*.id,
                    allPersons    : userService.findPersons(["Last${marker}"] as String[]).size(),
                    like          : userService.findUsers("manager${marker}", [status: 'true'])*.id,
                    roleType      : userService.findUsersByRoleType(RoleType.ROLE_BROWSER)*.id,
                    roleTypes     : userService.findUsersByRoleTypes(Location.get(depot.id), [RoleType.ROLE_MANAGER])*.id,
                    admins        : userService.getAllAdminUsers()*.username,
            ]
        }

        then:
        result.byTerm == [browser.id, manager.id] as Set
        result.byRole == [browser.id]
        result.byLocationRole == [manager.id]
        result.persons == [browser.id, manager.id]
        result.allPersons == 2
        result.like == [manager.id]
        result.roleType.contains(browser.id)
        !result.roleType.contains(manager.id)
        result.roleTypes.contains(manager.id)
        result.admins instanceof List
    }

    void 'the signup default roles are given to a new user, who becomes active'() {
        given:
        def previous = grailsApplication.config.openboxes.signup.defaultRoles
        grailsApplication.config.openboxes.signup.defaultRoles = 'ROLE_BROWSER'
        User user = newUser("signup${marker}", [], [:])
        committed {
            User saved = User.get(user.id)
            saved.active = false
            saved.save(flush: true)
        }

        when:
        Map result = asAdminAt(depot) {
            User saved = User.get(user.id)
            userService.assignDefaultRoles(saved)
            [roles: saved.roles*.roleType, active: saved.active]
        }

        then:
        result.roles == [RoleType.ROLE_BROWSER]
        result.active

        cleanup:
        grailsApplication.config.openboxes.signup.defaultRoles = previous
    }

}
