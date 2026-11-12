package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class AuthorizationServiceSpec extends Specification implements ServiceUnitTest<AuthorizationService>, DataTest {

    Location facilityA
    Location facilityB

    Map<RoleType, Role> roles = [:]
    Map<String, User> users = [:]

    void setupSpec() {
        mockDomain(Location)
        mockDomain(LocationRole)
        mockDomain(Role)
        mockDomain(User)
    }

    void setup() {
        facilityA = new Location(name: 'Facility A').save(validate: false)
        facilityB = new Location(name: 'Facility B').save(validate: false)

        [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_ASSISTANT,
         RoleType.ROLE_BROWSER, RoleType.ROLE_AUTHENTICATED, RoleType.ROLE_INVOICE,
         RoleType.ROLE_PURCHASE_APPROVER].each { RoleType roleType ->
            roles[roleType] = new Role(roleType: roleType, name: roleType.name()).save(validate: false)
        }

        // Global roles apply at every location
        users.globalBrowser = createUser('globalBrowser', [RoleType.ROLE_BROWSER])
        users.globalManager = createUser('globalManager', [RoleType.ROLE_MANAGER])
        users.globalSuperuser = createUser('globalSuperuser', [RoleType.ROLE_SUPERUSER])
        users.managerWithInvoice = createUser('managerWithInvoice', [RoleType.ROLE_MANAGER, RoleType.ROLE_INVOICE])

        // Only a location role at facility A
        users.browserAtA = createUser('browserAtA', [], [(facilityA): RoleType.ROLE_BROWSER])
        users.managerAtA = createUser('managerAtA', [], [(facilityA): RoleType.ROLE_MANAGER])

        // Authenticated globally, Manager at A
        users.authenticatedManagerAtA = createUser('authenticatedManagerAtA',
                [RoleType.ROLE_AUTHENTICATED], [(facilityA): RoleType.ROLE_MANAGER])

        users.inactiveManager = createUser('inactiveManager', [RoleType.ROLE_MANAGER])
        users.inactiveManager.active = false
        users.inactiveManager.save(validate: false)
    }

    private User createUser(String username, List<RoleType> globalRoles, Map<Location, RoleType> locationRoles = [:]) {
        User user = new User(username: username, password: 'pass', firstName: username, lastName: 'Test',
                email: "${username}@test.com", active: true)
        globalRoles.each { user.addToRoles(roles[it]) }
        user.save(validate: false)
        locationRoles.each { Location location, RoleType roleType ->
            user.addToLocationRoles(new LocationRole(user: user, location: location, role: roles[roleType]))
        }
        return user.save(validate: false)
    }

    private Location locationFor(String name) {
        return [A: facilityA, B: facilityB, none: null][name]
    }

    void "#username at facility #facility with minimum role #minimumRole -> #expected"() {
        expect:
        service.hasRole(users[username], locationFor(facility), minimumRole) == expected

        where:
        username                   | facility | minimumRole              || expected
        // location roles only count at their own location
        'browserAtA'               | 'A'      | RoleType.ROLE_BROWSER    || true
        'browserAtA'               | 'B'      | RoleType.ROLE_BROWSER    || false
        'browserAtA'               | 'none'   | RoleType.ROLE_BROWSER    || false
        'browserAtA'               | 'A'      | RoleType.ROLE_MANAGER    || false
        'managerAtA'               | 'A'      | RoleType.ROLE_BROWSER    || true
        'managerAtA'               | 'B'      | RoleType.ROLE_BROWSER    || false
        'authenticatedManagerAtA'  | 'A'      | RoleType.ROLE_MANAGER    || true
        'authenticatedManagerAtA'  | 'B'      | RoleType.ROLE_BROWSER    || false
        'authenticatedManagerAtA'  | 'B'      | RoleType.ROLE_AUTHENTICATED || true
        // global roles apply everywhere, including when no location is known
        'globalBrowser'            | 'B'      | RoleType.ROLE_BROWSER    || true
        'globalBrowser'            | 'none'   | RoleType.ROLE_BROWSER    || true
        'globalBrowser'            | 'A'      | RoleType.ROLE_ASSISTANT  || false
        'globalManager'            | 'B'      | RoleType.ROLE_ASSISTANT  || true
        'globalSuperuser'          | 'B'      | RoleType.ROLE_ADMIN      || true
        // no minimum role means any active user passes
        'browserAtA'               | 'B'      | null                     || true
        // inactive users never pass
        'inactiveManager'          | 'A'      | RoleType.ROLE_BROWSER    || false
    }

    void "supplemental roles require an exact match: #username with #supplementalRoles -> #expected"() {
        expect:
        service.hasRole(users[username], facilityA, null, supplementalRoles) == expected

        where:
        username             | supplementalRoles                   || expected
        'managerWithInvoice' | [RoleType.ROLE_INVOICE]             || true
        'globalManager'      | [RoleType.ROLE_INVOICE]             || false
        // RoleType.expand() would let any primary role satisfy these; we must not
        'globalSuperuser'    | [RoleType.ROLE_PURCHASE_APPROVER]   || false
        'globalBrowser'      | [RoleType.ROLE_PURCHASE_APPROVER]   || false
        // any one of the listed roles is enough
        'managerWithInvoice' | [RoleType.ROLE_PURCHASE_APPROVER, RoleType.ROLE_INVOICE] || true
    }

    void "minimum and supplemental roles must both be satisfied"() {
        expect:
        service.hasRole(users.managerWithInvoice, facilityA, RoleType.ROLE_MANAGER, [RoleType.ROLE_INVOICE])
        !service.hasRole(users.managerWithInvoice, facilityA, RoleType.ROLE_ADMIN, [RoleType.ROLE_INVOICE])
        !service.hasRole(users.globalManager, facilityA, RoleType.ROLE_MANAGER, [RoleType.ROLE_INVOICE])
    }

    void "a null or unsaved user never passes"() {
        expect:
        !service.hasRole(null, facilityA, null)
        !service.hasRole(new User(username: 'transient'), facilityA, null)
    }
}
