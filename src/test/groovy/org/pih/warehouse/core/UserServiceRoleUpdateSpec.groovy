package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.localization.MessageLocalizer

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Role changes made through UserService.updateUser: a user can only grant or
 * remove roles at or below their own highest role, whether the roles arrive
 * as requested role ids or as location role pairs. Users below Assistant
 * cannot change roles at all.
 */
@Unroll
class UserServiceRoleUpdateSpec extends Specification implements ServiceUnitTest<UserService>, DataTest {

    Role superuserRole
    Role adminRole
    Role managerRole
    Role assistantRole
    Role browserRole

    User superuser
    User admin
    User manager
    User assistant
    User browser

    Location depot

    Closure doWithConfig() {{ config ->
        config.openboxes.locale.localizationModeLocale = 'ac'
    }}

    void setupSpec() {
        mockDomain(Location)
        mockDomain(LocationRole)
        mockDomain(Role)
        mockDomain(User)
    }

    void setup() {
        superuserRole = new Role(roleType: RoleType.ROLE_SUPERUSER, name: 'Superuser').save(validate: false)
        adminRole = new Role(roleType: RoleType.ROLE_ADMIN, name: 'Admin').save(validate: false)
        managerRole = new Role(roleType: RoleType.ROLE_MANAGER, name: 'Manager').save(validate: false)
        assistantRole = new Role(roleType: RoleType.ROLE_ASSISTANT, name: 'Assistant').save(validate: false)
        browserRole = new Role(roleType: RoleType.ROLE_BROWSER, name: 'Browser').save(validate: false)

        superuser = newUser('superuser', superuserRole)
        admin = newUser('admin', adminRole)
        manager = newUser('manager', managerRole)
        assistant = newUser('assistant', assistantRole)
        browser = newUser('browser', browserRole)

        depot = new Location(name: 'Depot').save(validate: false)

        // With no current location, the requesting user's highest role is
        // taken from their global roles (see the FIXME in UserService).
        service.authService = Stub(AuthService) {
            getCurrentLocation() >> null
        }
        service.messageLocalizer = Stub(MessageLocalizer) {
            localize(_ as String, _) >> { String code, Object[] args -> code }
            localize(_ as String) >> { String code -> code }
        }
    }

    private static User newUser(String username, Role role) {
        User user = new User(username: username, password: 'password', passwordConfirm: 'password',
            firstName: username, lastName: 'User', email: "${username}@test.com")
        user.addToRoles(role)
        return user.save(validate: false)
    }

    private User userByName(String name) {
        switch (name) {
            case 'superuser': return superuser
            case 'admin':     return admin
            case 'manager':   return manager
            case 'assistant': return assistant
            case 'browser':   return browser
        }
    }

    private Role roleByName(String name) {
        switch (name) {
            case 'Superuser': return superuserRole
            case 'Admin':     return adminRole
            case 'Manager':   return managerRole
            case 'Assistant': return assistantRole
            case 'Browser':   return browserRole
        }
    }

    void "should not let #requester give #target the '#role' role through the requested roles"() {
        given:
        User requestingUser = userByName(requester)
        User targetUser = userByName(target)
        Set<Role> rolesBefore = targetUser.roles.collect() as Set

        when:
        service.updateUser(targetUser.id, requestingUser.id,
            (targetUser.roles*.id + roleByName(role).id) as List<String>, [:])

        then:
        thrown(ValidationException)
        targetUser.roles as Set == rolesBefore

        where:
        requester   | target      | role
        'assistant' | 'assistant' | 'Superuser'
        'assistant' | 'assistant' | 'Admin'
        'assistant' | 'assistant' | 'Manager'
        'assistant' | 'browser'   | 'Admin'
        'manager'   | 'manager'   | 'Admin'
        'manager'   | 'browser'   | 'Superuser'
        'admin'     | 'admin'     | 'Superuser'
        'admin'     | 'browser'   | 'Superuser'
        'browser'   | 'browser'   | 'Assistant'
        'browser'   | 'assistant' | 'Browser'
    }

    void "should not let #requester give #target the '#role' role at a location"() {
        given:
        User requestingUser = userByName(requester)
        User targetUser = userByName(target)

        when:
        service.updateUser(targetUser.id, requestingUser.id, [],
            [locationRolePairs: [(depot.id): roleByName(role).id]])

        then:
        thrown(ValidationException)
        !targetUser.locationRoles

        where:
        requester   | target      | role
        'assistant' | 'assistant' | 'Superuser'
        'assistant' | 'assistant' | 'Admin'
        'assistant' | 'browser'   | 'Manager'
        'manager'   | 'manager'   | 'Admin'
        'admin'     | 'admin'     | 'Superuser'
        'browser'   | 'browser'   | 'Browser'
    }

    void "should not let #requester remove the '#role' role from #target"() {
        given:
        User requestingUser = userByName(requester)
        User targetUser = userByName(target)
        Set<Role> rolesBefore = targetUser.roles.collect() as Set

        when: 'the requested roles drop the target user\'s only role in favour of Browser'
        service.updateUser(targetUser.id, requestingUser.id, [browserRole.id], [:])

        then:
        thrown(ValidationException)
        targetUser.roles as Set == rolesBefore

        where:
        requester   | target      | role
        'assistant' | 'manager'   | 'Manager'
        'manager'   | 'admin'     | 'Admin'
        'admin'     | 'superuser' | 'Superuser'
    }

    void "should let #requester give #target the '#role' role"() {
        given:
        User requestingUser = userByName(requester)
        User targetUser = userByName(target)

        when:
        User updated = service.updateUser(targetUser.id, requestingUser.id,
            (targetUser.roles*.id + roleByName(role).id) as List<String>, [:])

        then:
        updated.roles.contains(roleByName(role))
        updated.roles.contains(browserRole)

        where:
        requester   | target    | role
        'assistant' | 'browser' | 'Assistant'
        'manager'   | 'browser' | 'Manager'
        'admin'     | 'browser' | 'Admin'
        'superuser' | 'browser' | 'Superuser'
    }

    void "should let #requester give #target the '#role' role at a location"() {
        given:
        User requestingUser = userByName(requester)
        User targetUser = userByName(target)

        when:
        User updated = service.updateUser(targetUser.id, requestingUser.id, [],
            [locationRolePairs: [(depot.id): roleByName(role).id]])

        then:
        updated.locationRoles*.role == [roleByName(role)]
        updated.locationRoles*.location == [depot]

        where:
        requester   | target    | role
        'assistant' | 'browser' | 'Assistant'
        'manager'   | 'browser' | 'Manager'
        'admin'     | 'browser' | 'Admin'
        'superuser' | 'browser' | 'Superuser'
    }
}
