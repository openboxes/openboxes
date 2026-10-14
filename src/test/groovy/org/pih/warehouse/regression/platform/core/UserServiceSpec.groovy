package org.pih.warehouse.regression.platform.core

import org.pih.warehouse.core.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.testing.web.GrailsWebUnitTest
import grails.validation.ValidationException
import org.apache.http.auth.AuthenticationException
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.PasswordCodec
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * The role questions the user service answers (global roles plus the roles a user has at the current location),
 * password changes, location role assignment and the dashboard configuration.
 */
class UserServiceSpec extends Specification implements ServiceUnitTest<UserService>, DataTest, GrailsWebUnitTest {

    AuthService authService = new AuthService()
    Location depot
    Location clinic
    Map<RoleType, Role> roles = [:]

    void setupSpec() {
        mockDomains(Location, LocationRole, Role, User, Person)
        mockCodec(PasswordCodec)
    }

    void setup() {
        [RoleType.ROLE_SUPERUSER, RoleType.ROLE_ADMIN, RoleType.ROLE_MANAGER, RoleType.ROLE_ASSISTANT, RoleType.ROLE_BROWSER,
         RoleType.ROLE_REQUESTOR, RoleType.ROLE_FINANCE, RoleType.ROLE_INVOICE, RoleType.ROLE_PURCHASE_APPROVER,
         RoleType.ROLE_PRODUCT_MANAGER].each { RoleType roleType ->
            roles[roleType] = new Role(roleType: roleType, name: roleType.name()).save(validate: false, flush: true)
        }
        depot = new Location(name: 'Depot').save(validate: false, flush: true)
        clinic = new Location(name: 'Clinic').save(validate: false, flush: true)
        service.authService = authService
        // OBAF's UserService may have no messageLocalizer: set it only where the property exists.
        if (service.metaClass.hasProperty(service, 'messageLocalizer')) {
            service.messageLocalizer = Stub(MessageLocalizer) {
                localize(*_) >> { args -> args[0] }
            }
        }
        authService.setCurrentLocation(depot)
    }

    void cleanup() {
        authService.setCurrentLocation(null)
        authService.setCurrentUser(null)
    }

    private User user(String username, List<RoleType> globalRoles, Map<Location, RoleType> locationRoles = [:]) {
        User user = new User(username: username, password: 'secret', passwordConfirm: 'secret', firstName: username,
                lastName: 'User', email: "${username}@example.com")
        globalRoles.each { user.addToRoles(roles[it]) }
        locationRoles.each { Location location, RoleType roleType ->
            user.addToLocationRoles(new LocationRole(user: user, location: location, role: roles[roleType]))
        }
        return user.save(validate: false, flush: true)
    }

    @Unroll
    void 'role questions for a user with #globalRoles and #depotRoles at the current location'() {
        given:
        User user = user('u', globalRoles, depotRoles.collectEntries { [(depot): it] })

        expect:
        service.isSuperuser(user) == superuser
        service.isUserAdmin(user) == admin
        service.isUserManager(user) == manager
        service.isUserRequestor(user) == requestor
        service.canUserBrowse(user) == browse
        service.hasRoleFinance(user) == finance
        service.hasRoleInvoice(user) == finance
        service.hasRolePurchaseApprover(user) == finance
        service.hasRoleProductManager(user) == finance
        service.hasRoleBrowser(user) == browserRole

        where:
        globalRoles                                                                  | depotRoles                     || superuser | admin | manager | requestor | browse | finance | browserRole
        [RoleType.ROLE_SUPERUSER]                                                    | []                             || true      | true  | true    | false     | true   | false   | false
        [RoleType.ROLE_ADMIN]                                                        | []                             || false     | true  | true    | false     | true   | false   | false
        []                                                                           | [RoleType.ROLE_MANAGER]        || false     | false | true    | false     | true   | false   | false
        [RoleType.ROLE_BROWSER]                                                      | [RoleType.ROLE_REQUESTOR]      || false     | false | false   | true      | true   | false   | true
        [RoleType.ROLE_FINANCE, RoleType.ROLE_INVOICE, RoleType.ROLE_PURCHASE_APPROVER,
         RoleType.ROLE_PRODUCT_MANAGER]                                              | []                             || false     | false | false   | false     | false  | true    | false
    }

    void 'every role question is no for a missing user'() {
        expect:
        !service.isSuperuser(null)
        !service.isUserAdmin(null)
        !service.isUserManager(null)
        !service.isUserRequestor(null)
        !service.canUserBrowse(null)
        !service.hasRoleFinance((User) null)
        !service.hasRoleInvoice(null)
        !service.hasRolePurchaseApprover(null)
        !service.hasRoleProductManager(null)
        !service.hasRoleBrowser(null)
        !service.hasRoleRequestorInAnyLocations(null)
        !service.hasHighestRole(null, depot.id, RoleType.ROLE_ADMIN)
    }

    void 'roles held at another location count only where the question is about any location'() {
        given:
        User user = user('u', [], [(clinic): RoleType.ROLE_REQUESTOR])

        expect:
        !service.isUserRequestor(user)
        service.hasRoleRequestorInAnyLocations(user)
        service.hasAnyRoles(user, [RoleType.ROLE_BROWSER]) == false
    }

    void 'the highest role is compared at the given location'() {
        given:
        User user = user('u', [RoleType.ROLE_BROWSER], [(depot): RoleType.ROLE_MANAGER])

        expect:
        service.hasHighestRole(user, depot.id, RoleType.ROLE_MANAGER)
        !service.hasHighestRole(user, depot.id, RoleType.ROLE_BROWSER)
        service.hasHighestRole(user, clinic.id, RoleType.ROLE_BROWSER)
    }

    void 'isUserInRole accepts the given role types and the ones they imply'() {
        given:
        User admin = user('admin', [RoleType.ROLE_ADMIN])
        User browser = user('browser', [RoleType.ROLE_BROWSER])

        expect:
        service.isUserInRole(admin, RoleType.ROLE_ADMIN)
        service.isUserInRole(admin, [RoleType.ROLE_MANAGER, RoleType.ROLE_ADMIN])
        !service.isUserInRole(browser, RoleType.ROLE_ADMIN)
        service.isUserInRole(browser.id, [RoleType.ROLE_BROWSER])
        service.isUserInAllRoles(admin.id, [RoleType.ROLE_ADMIN], depot.id)
        !service.isUserInAllRoles(admin.id, [RoleType.ROLE_ADMIN, RoleType.ROLE_FINANCE], depot.id)
    }

    void 'the finance role of the current user'() {
        given:
        User finance = user('finance', [RoleType.ROLE_FINANCE])
        User browser = user('browser', [RoleType.ROLE_BROWSER])

        when:
        authService.setCurrentUser(finance)

        then:
        service.hasRoleFinance()
        service.assertCurrentUserHasRoleFinance() == null

        when:
        authService.setCurrentUser(browser)
        service.assertCurrentUserHasRoleFinance()

        then:
        !service.hasRoleFinance()
        IllegalStateException e = thrown()
        e.message == 'User browser must have ROLE_FINANCE role'
    }

    void 'default roles may be named by role name or role type'() {
        given:
        roles[RoleType.ROLE_FINANCE].name = 'Financial User'
        roles[RoleType.ROLE_FINANCE].save(validate: false, flush: true)
        roles[RoleType.ROLE_BROWSER].name = 'Browser'
        roles[RoleType.ROLE_BROWSER].save(validate: false, flush: true)

        when:
        Role[] extracted = service.extractDefaultRoles('Financial User, ROLE_BROWSER')

        then:
        extracted*.roleType == [RoleType.ROLE_FINANCE, RoleType.ROLE_BROWSER]
    }

    void 'the dashboard configuration starts from the configured dashboard and keeps the user\'s personal one'() {
        given:
        config.openboxes.dashboardConfig = [
                mainDashboardId : 'main',
                dashboards      : [main: [personal: [widgets: ['default']]], other: [widgets: ['other']]],
                dashboardWidgets: [widget: [title: 'Widget']],
        ]
        User user = user('u', [])
        service.updateDashboardConfig(user, [personal: [widgets: ['mine']]])

        when:
        Map main = service.getDashboardConfig(user, 'main')
        Map other = service.getDashboardConfig(user, 'other')

        then:
        main.dashboard.personal.widgets == ['mine']
        main.dashboardWidgets.widget.title == 'Widget'
        other.dashboard.widgets == ['other']
    }
}
