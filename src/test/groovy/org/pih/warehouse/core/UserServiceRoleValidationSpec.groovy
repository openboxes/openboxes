package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.localization.MessageLocalizer

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class UserServiceRoleValidationSpec extends Specification implements ServiceUnitTest<UserService>, DataTest {

    Role adminRole
    Role browserRole
    Role managerRole
    Role superuserRole
    Role financeRole
    Role invoiceRole

    User admin
    User manager
    User superuser
    User browser

    void setupSpec() {
        mockDomain(Location)
        mockDomain(LocationRole)
        mockDomain(Role)
        mockDomain(User)
    }

    void setup() {
        adminRole = new Role(roleType: RoleType.ROLE_ADMIN, name: 'Admin').save(validate: false)
        browserRole = new Role(roleType: RoleType.ROLE_BROWSER, name: 'Browser').save(validate: false)
        managerRole = new Role(roleType: RoleType.ROLE_MANAGER, name: 'Manager').save(validate: false)
        superuserRole = new Role(roleType: RoleType.ROLE_SUPERUSER, name: 'Superuser').save(validate: false)
        // Supplementary roles, both sortOrder 100 (see RoleType) - the collision
        // that triggered OBPIH-7904 when diffed with Groovy's collection minus.
        financeRole = new Role(roleType: RoleType.ROLE_FINANCE, name: 'Financial User').save(validate: false)
        invoiceRole = new Role(roleType: RoleType.ROLE_INVOICE, name: 'Invoice user').save(validate: false)

        admin = new User(username: 'admin', password: 'pass', passwordConfirm: 'pass',
            firstName: 'Admin', lastName: 'User', email: 'admin@test.com')
        admin.addToRoles(adminRole)
        admin.save(validate: false)

        manager = new User(username: 'manager', password: 'pass', passwordConfirm: 'pass',
            firstName: 'Manager', lastName: 'User', email: 'mgr@test.com')
        manager.addToRoles(managerRole)
        manager.save(validate: false)

        superuser = new User(username: 'superuser', password: 'pass', passwordConfirm: 'pass',
            firstName: 'Super', lastName: 'User', email: 'su@test.com')
        superuser.addToRoles(superuserRole)
        superuser.save(validate: false)

        browser = new User(username: 'browser', password: 'pass', passwordConfirm: 'pass',
            firstName: 'Browser', lastName: 'User', email: 'br@test.com')
        browser.addToRoles(browserRole)
        browser.save(validate: false)

        /*
         * If currentLocation is null, requestingUser.getHighestRole() can
         * still check global roles. Not ideal (see FIXME in UserService
         * for more details), but it's enough for testing purposes, for now.
         */
        service.authService = Stub(AuthService) {
            getCurrentLocation() >> null
        }

        // offers the bare minimum for localized error messages to work
        service.messageLocalizer = Stub(MessageLocalizer) {
            localize(_ as String, _) >> { String code, Object[] args -> code }
            localize(_ as String) >> { String code -> code }
        }
    }

    private User userByName(String name) {
        switch (name) {
            case 'superuser': return superuser
            case 'admin':     return admin
            case 'manager':   return manager
            case 'browser':   return browser
            case 'null':      return null
        }
    }

    private Role roleByName(String name) {
        switch (name) {
            case 'Superuser': return superuserRole
            case 'Admin':     return adminRole
            case 'Manager':   return managerRole
            case 'Browser':   return browserRole
            case 'Finance User':   return financeRole
            case 'Invoice User':   return invoiceRole
        }
    }

    void "should #allowOrForbid #user to add or remove the '#role' role"() {
        expect:
        assert service.canAddOrRemoveRole(userByName(user), roleByName(role)) == allowed

        where:
        user        | role           | allowOrForbid || allowed
        'superuser' | 'Admin'        | 'allow'       || true
        'superuser' | 'Browser'      | 'allow'       || true
        'superuser' | 'Manager'      | 'allow'       || true
        'superuser' | 'Superuser'    | 'allow'       || true
        'superuser' | 'Finance User' | 'allow'       || true
        'superuser' | 'Invoice User' | 'allow'       || true
        'admin'     | 'Admin'        | 'allow'       || true
        'admin'     | 'Browser'      | 'allow'       || true
        'admin'     | 'Manager'      | 'allow'       || true
        'admin'     | 'Superuser'    | 'forbid'      || false
        'admin'     | 'Finance User' | 'allow'       || true
        'admin'     | 'Invoice User' | 'allow'       || true
        'manager'   | 'Admin'        | 'forbid'      || false
        'manager'   | 'Browser'      | 'allow'       || true
        'manager'   | 'Manager'      | 'allow'       || true
        'manager'   | 'Superuser'    | 'forbid'      || false
        'manager'   | 'Finance User' | 'allow'       || true
        'manager'   | 'Invoice User' | 'allow'       || true
        'Browser'   | 'Admin'        | 'forbid'      || false
        'Browser'   | 'Browser'      | 'forbid'      || false
        'Browser'   | 'Manager'      | 'forbid'      || false
        'Browser'   | 'Superuser'    | 'forbid'      || false
        'Browser'   | 'Finance User' | 'forbid'      || false
        'Browser'   | 'Invoice User' | 'forbid'      || false
        'null'      | 'Browser'      | 'forbid'      || false
    }

    void "should allow an update that leaves the roles unchanged"() {
        when:
        service.checkCanAddOrRemoveRoles(admin, manager, [adminRole, managerRole], [adminRole, managerRole])
        service.checkCanAddOrRemoveRoles(manager, admin, [adminRole, managerRole], [adminRole, managerRole])

        then:
        noExceptionThrown()
    }

    void "should allow an update for a user with no roles before or after"() {
        when:
        service.checkCanAddOrRemoveRoles(superuser, admin, [], [])
        service.checkCanAddOrRemoveRoles(manager, admin, [], [])
        service.checkCanAddOrRemoveRoles(admin, manager, [], [])

        then:
        noExceptionThrown()
    }

    void "should reject an update made by non manager user"() {
        when:
        service.checkCanAddOrRemoveRoles(browser, browser, [], [adminRole])

        then:
        thrown(ValidationException)
    }

    void "should reject an update whose params contain a 'roles' key"() {
        when:
        service.updateUser(admin.id, superuser.id, [], [roles: [adminRole.id]])

        then:
        thrown(ValidationException)
    }

    void "should reject an update whose params contain a 'roles[].id' key"() {
        when:
        service.updateUser(admin.id, superuser.id, [], ['roles[0].id': superuserRole.id])

        then:
        thrown(ValidationException)
    }

    void "should reject an update whose params contain a 'locationRoles' key"() {
        when:
        service.updateUser(admin.id, superuser.id, [], [locationRoles: [adminRole.id]])

        then:
        thrown(ValidationException)
    }

    void "should reject an update whose params contain a 'locationRoles[].id' key"() {
        when:
        service.updateUser(admin.id, superuser.id, [], ['locationRoles[0].id': adminRole.id])

        then:
        thrown(ValidationException)
    }

    void "should remove a role while keeping another that shares its sortOrder"() {
        given: 'a user with two supplementary roles that share a sortOrder'
        manager.addToRoles(financeRole)
        manager.addToRoles(invoiceRole)
        manager.save(validate: false)

        when: 'a superuser requests the same roles minus invoice'
        service.validateAndApplyRoleChanges(superuser, manager, [managerRole.id, financeRole.id])

        then: 'invoice is removed'
        !manager.roles.contains(invoiceRole)

        and: 'the manager and finance roles are kept'
        manager.roles.contains(managerRole)
        manager.roles.contains(financeRole)
    }

    void "should add a role that shares a sortOrder with one the user already has"() {
        given: 'a user that already has one supplementary role'
        manager.addToRoles(financeRole)
        manager.save(validate: false)

        when: 'a superuser requests an additional role with the same sortOrder'
        service.validateAndApplyRoleChanges(superuser, manager, [managerRole.id, financeRole.id, invoiceRole.id])

        then: 'both supplementary roles are present'
        manager.roles.contains(financeRole)
        manager.roles.contains(invoiceRole)
    }
}
