package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.testing.gorm.DataTest
import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService

class AuthTagLibSpec extends Specification implements TagLibUnitTest<AuthTagLib>, DataTest {

    Location depot
    Location requestOnlyWard
    User user
    UserService userService = Mock(UserService)

    void setupSpec() {
        mockDomains(Location, LocationType, User, Role, LocationRole)
    }

    void setup() {
        LocationType type = new LocationType(name: 'Depot', locationTypeCode: LocationTypeCode.DEPOT).save(validate: false)
        depot = new Location(name: 'Depot', locationType: type, supportedActivities: [
                ActivityCode.MANAGE_INVENTORY.id, ActivityCode.SUBMIT_REQUEST.id, ActivityCode.PICK_STOCK.id] as Set)
                .save(validate: false, flush: true)
        requestOnlyWard = new Location(name: 'Ward', locationType: type,
                supportedActivities: [ActivityCode.SUBMIT_REQUEST.id] as Set).save(validate: false, flush: true)
        user = new User(username: 'jo', firstName: 'Jo', lastName: 'Doe').save(validate: false, flush: true)
        session.warehouse = depot
        session.user = user
        tagLib.userService = userService
    }

    void 'supports renders the body only when the location supports the activities'() {
        expect:
        applyTemplate('<g:supports activityCode="MANAGE_INVENTORY">yes</g:supports>') == 'yes'
        applyTemplate('<g:supports activityCode="SEND_STOCK">yes</g:supports>') == ''
        applyTemplate('<g:supports activitiesAny="${[\'SEND_STOCK\', \'PICK_STOCK\']}">yes</g:supports>') == 'yes'
        applyTemplate('<g:supports activitiesAny="${[\'SEND_STOCK\', \'RECEIVE_STOCK\']}">yes</g:supports>') == ''
        applyTemplate('<g:supports activitiesAll="${[\'MANAGE_INVENTORY\', \'PICK_STOCK\']}">yes</g:supports>') == 'yes'
        applyTemplate('<g:supports activitiesAll="${[\'MANAGE_INVENTORY\', \'SEND_STOCK\']}">yes</g:supports>') == ''
        applyTemplate('<g:supports location="${l}" activityCode="MANAGE_INVENTORY">yes</g:supports>',
                [l: requestOnlyWard.id]) == ''
    }

    void 'supports fails without a location'() {
        when:
        applyTemplate('<g:supports location="missing" activityCode="MANAGE_INVENTORY">yes</g:supports>')

        then:
        Exception e = thrown()
        e.message.contains('Please choose a warehouse')
    }

    void 'hideIfIsNonInventoryManagedAndCanSubmitRequest hides the body for request-only locations'() {
        expect:
        applyTemplate('<g:hideIfIsNonInventoryManagedAndCanSubmitRequest>shown</g:hideIfIsNonInventoryManagedAndCanSubmitRequest>') == 'shown'

        when:
        session.warehouse = requestOnlyWard

        then:
        applyTemplate('<g:hideIfIsNonInventoryManagedAndCanSubmitRequest>shown</g:hideIfIsNonInventoryManagedAndCanSubmitRequest>') == ''
    }

    void 'role tags render the body only when the user service grants the role'() {
        when:
        String granted = applyTemplate("<g:${tag}>body</g:${tag}>")

        then:
        1 * userService."${check}"(user) >> true
        granted == 'body'

        when:
        String denied = applyTemplate("<g:${tag}>body</g:${tag}>")

        then:
        1 * userService."${check}"(user) >> false
        denied == ''

        where:
        tag                     | check
        'isSuperuser'           | 'isSuperuser'
        'isUserAdmin'           | 'isUserAdmin'
        'isUserManager'         | 'isUserManager'
        'hasRoleFinance'        | 'hasRoleFinance'
        'hasRoleApprover'       | 'hasRolePurchaseApprover'
        'hasRoleInvoice'        | 'hasRoleInvoice'
        'hasRoleProductManager' | 'hasRoleProductManager'
    }

    void 'hasRoleFinance shows the access denied text to other users'() {
        when:
        String html = applyTemplate('<g:hasRoleFinance onAccessDenied="hidden">amount</g:hasRoleFinance>')

        then:
        1 * userService.hasRoleFinance(user) >> false
        html == 'hidden'
    }

    void 'isUserInAllRoles and isUserInRole check the logged in user'() {
        when:
        String all = applyTemplate('<g:isUserInAllRoles roles="${r}" location="${l}">all</g:isUserInAllRoles>',
                [r: [RoleType.ROLE_MANAGER], l: depot.id])
        String any = applyTemplate('<g:isUserInRole roles="${r}">any</g:isUserInRole>', [r: [RoleType.ROLE_ADMIN]])

        then:
        1 * userService.isUserInAllRoles(user.id, [RoleType.ROLE_MANAGER], depot.id) >> true
        1 * userService.isUserInRole(user.id, [RoleType.ROLE_ADMIN]) >> false
        all == 'all'
        any == ''

        when: 'nobody is logged in'
        session.user = null

        then:
        applyTemplate('<g:isUserInAllRoles roles="${r}">all</g:isUserInAllRoles>', [r: []]) == ''
        applyTemplate('<g:isUserInRole roles="${r}">any</g:isUserInRole>', [r: []]) == ''
        0 * userService._
    }

    void 'highest role tags compare against the authenticated role'() {
        when:
        String only = applyTemplate('<g:hasHighestRoleAuthenticated>basic</g:hasHighestRoleAuthenticated>')
        String higher = applyTemplate('<g:hasHigherRoleThanAuthenticated>more</g:hasHigherRoleThanAuthenticated>')

        then:
        2 * userService.hasHighestRole(user, depot.id, RoleType.ROLE_AUTHENTICATED) >> true
        only == 'basic'
        higher == ''
    }

    void 'userRole shows the highest role of a user at the current location'() {
        given:
        Role manager = new Role(roleType: RoleType.ROLE_MANAGER, name: 'Manager').save(validate: false, flush: true)
        Role browser = new Role(roleType: RoleType.ROLE_BROWSER, name: 'Browser').save(validate: false, flush: true)
        user.roles = [browser] as Set
        user.locationRoles = [new LocationRole(user: user, location: depot, role: manager).save(validate: false)]
        user.save(validate: false, flush: true)

        expect:
        applyTemplate('<g:userRole user="${u}"/>', [u: user]) == RoleType.ROLE_MANAGER.name
    }

    void 'userPhoto renders the photo template for the user'() {
        given:
        views['/taglib/_userPhoto.gsp'] = 'photo of ${userInstance.username}'

        expect:
        applyTemplate('<g:userPhoto user="${u}"/>', [u: user]) == 'photo of jo'
    }
}
