package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.testing.gorm.DataTest
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService

class LinkTagLibSpec extends Specification implements TagLibUnitTest<LinkTagLib>, DataTest {

    UserService userService = Mock(UserService)
    User user = new User(username: 'jo')

    void setupSpec() {
        mockDomain(User)
        mockTagLib(ApplicationTagLib)
    }

    void setup() {
        config.openboxes.security.rbac.rules = [
                [controller: '*', actions: ['delete'], accessRules: [minimumRequiredRole: RoleType.ROLE_SUPERUSER]],
                [controller: 'order', actions: ['deleteDocument'], accessRules: [minimumRequiredRole: RoleType.ROLE_ADMIN]],
        ]
        tagLib.userService = userService
        session.user = user
    }

    private String link(Map attrs) {
        return tagLib.link(attrs, { 'Go' }).toString()
    }

    void 'a link the user may follow renders normally'() {
        when:
        String html = link(controller: 'product', action: 'show', id: '1')

        then:
        html == '<a href="/product/show/1">Go</a>'
    }

    void 'actions open to anonymous users are never disabled'() {
        when:
        String html = link(controller: 'auth', action: 'login')

        then:
        0 * userService._
        html == '<a href="/auth/login">Go</a>'
    }

    void 'a change action is disabled for users who are not managers'() {
        when:
        String html = link(controller: 'product', action: 'update', id: '1')

        then:
        1 * userService.isUserManager(user) >> manager
        (html.contains("onclick=\"alert(&#39;Access denied&#39;); return false;\"") ||
                html.contains("onclick=\"alert('Access denied'); return false;\"")) == !manager

        where:
        manager << [true, false]
    }

    void 'requestor-or-manager actions also accept requestors'() {
        given:
        Map original = RoleInterceptor.requestorOrManagerActions
        RoleInterceptor.requestorOrManagerActions = [stockRequest: ['createRequest']]

        when:
        String html = link(controller: 'stockRequest', action: 'createRequest')

        then:
        1 * userService.isUserManager(user) >> false
        1 * userService.isUserRequestor(user) >> true
        !html.contains('onclick')

        cleanup:
        RoleInterceptor.requestorOrManagerActions = original
    }

    void 'admin actions require an admin and rbac rules require the minimum role'() {
        when:
        String admin = link(controller: 'user', action: 'list')

        then:
        1 * userService.isUserAdmin(user) >> false
        admin.contains('onclick')

        when:
        String ruled = link(controller: 'order', action: 'deleteDocument', disabledMessage: 'Admins only')

        then:
        1 * userService.isUserInRole(user, RoleType.ROLE_ADMIN) >> false
        _ * userService.isUserManager(user) >> true
        ruled.contains('Admins only')
    }

    void 'a disabled link always alerts'() {
        expect:
        link(controller: 'product', action: 'show', disabled: true).contains('onclick')
    }
}
