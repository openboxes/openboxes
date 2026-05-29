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

    User admin
    User manager
    User superuser

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
            case 'null':      return null
        }
    }

    private Role roleByName(String name) {
        switch (name) {
            case 'Superuser': return superuserRole
            case 'Admin':     return adminRole
            case 'Manager':   return managerRole
            case 'Browser':   return browserRole
        }
    }

    void "canAddOrRemoveRole #allows #user to add or remove role '#role'"() {
        expect:
        assert service.canAddOrRemoveRole(userByName(user), roleByName(role)) == (allows == 'allows')

        where:
        user        | role        || allows
        'superuser' | 'Admin'     || 'allows'
        'superuser' | 'Browser'   || 'allows'
        'superuser' | 'Manager'   || 'allows'
        'superuser' | 'Superuser' || 'allows'
        'admin'     | 'Admin'     || 'allows'
        'admin'     | 'Browser'   || 'allows'
        'admin'     | 'Manager'   || 'allows'
        'admin'     | 'Superuser' || 'forbids'
        'manager'   | 'Admin'     || 'forbids'
        'manager'   | 'Browser'   || 'allows'
        'manager'   | 'Manager'   || 'allows'
        'manager'   | 'Superuser' || 'forbids'
        'null'      | 'Browser'   || 'forbids'
    }

    void "checkCanAddOrRemoveRoles passes when no roles change"() {
        when:
        service.checkCanAddOrRemoveRoles(admin, manager, [adminRole, managerRole], [adminRole, managerRole])
        service.checkCanAddOrRemoveRoles(manager, admin, [adminRole, managerRole], [adminRole, managerRole])

        then:
        noExceptionThrown()
    }

    void "checkCanAddOrRemoveRoles passes for empty before and after"() {
        when:
        service.checkCanAddOrRemoveRoles(superuser, admin, [], [])
        service.checkCanAddOrRemoveRoles(manager, admin, [], [])
        service.checkCanAddOrRemoveRoles(admin, manager, [], [])

        then:
        noExceptionThrown()
    }

    void "updateUser rejects params containing roles keys"() {
        when:
        service.updateUser(admin.id, superuser.id, [], [roles: [adminRole.id]])

        then:
        thrown(ValidationException)
    }

    void "updateUser rejects params containing roles[] keys"() {
        when:
        service.updateUser(admin.id, superuser.id, [], ['roles[0].id': superuserRole.id])

        then:
        thrown(ValidationException)
    }

    void "updateUser rejects params containing locationRoles keys"() {
        when:
        service.updateUser(admin.id, superuser.id, [], [locationRoles: [adminRole.id]])

        then:
        thrown(ValidationException)
    }

    void "updateUser rejects params containing locationRoles[] keys"() {
        when:
        service.updateUser(admin.id, superuser.id, [], ['locationRoles[0].id': adminRole.id])

        then:
        thrown(ValidationException)
    }
}
