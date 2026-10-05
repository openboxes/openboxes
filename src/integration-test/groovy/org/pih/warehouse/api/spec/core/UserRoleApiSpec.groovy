package org.pih.warehouse.api.spec.core

import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import io.restassured.path.json.JsonPath
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.core.UserRoleApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationRole
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User

class UserRoleApiSpec extends ApiSpec {

    @Autowired
    UserRoleApiWrapper userRoleApiWrapper

    @Shared
    User user

    @Shared
    String userPassword

    @Shared
    Location otherFacility

    @Override
    void setupData() {
        otherFacility = new LocationTestBuilder().asFacility().name("Test Facility ${UUID.randomUUID()}").build(true)

        userPassword = randomUtil.randomStringFieldValue("password")
        user = new User(
                username: randomUtil.randomStringFieldValue("username"),
                password: userPassword,
                passwordConfirm: userPassword,
                firstName: "Test",
                lastName: "User",
        )
        user.addToRoles(Role.findByRoleType(RoleType.ROLE_BROWSER))
        user.addToLocationRoles(new LocationRole(location: facility, role: Role.findByRoleType(RoleType.ROLE_MANAGER)))
        user.addToLocationRoles(new LocationRole(location: otherFacility, role: Role.findByRoleType(RoleType.ROLE_ASSISTANT)))
        user.save(flush: true, failOnError: true)
    }

    @Override
    void cleanupData() {
        User.get(user.id)?.delete()
    }

    void 'get user roles should return all global and location roles when no facility is given'() {
        when:
        JsonPath json = userRoleApiWrapper.getUserRolesOK(user.id)

        then:
        json.getList("data.roles.roleType") == [RoleType.ROLE_BROWSER.name()]
        json.getList("data.locationRoles.location.id") as Set == [facility.id, otherFacility.id] as Set
    }

    void 'get user roles should return all global roles but only the location roles at the given facility'() {
        when:
        JsonPath json = userRoleApiWrapper.getUserRolesOK(user.id, otherFacility.id)

        then:
        json.getList("data.roles.roleType") == [RoleType.ROLE_BROWSER.name()]
        json.getList("data.locationRoles.location.id") == [otherFacility.id]
        json.getList("data.locationRoles.role.roleType") == [RoleType.ROLE_ASSISTANT.name()]
    }

    void 'get user roles should fail when the user does not exist'() {
        expect:
        userRoleApiWrapper.api.getUserRoles(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'get user roles should fail when the facility does not exist'() {
        expect:
        userRoleApiWrapper.api.getUserRoles(user.id, INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'get user roles should be forbidden for a user who is not an admin'() {
        given: 'subsequent requests are made as the test user, who is only a manager at the facility'
        Cookie nonAdminCookie = authApiWrapper.loginOK(user.username, userPassword, facility.id)
                .getDetailedCookie("JSESSIONID")
        authenticatedApiContext.loadContext(new RequestSpecBuilder()
                .addCookie(nonAdminCookie)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .build())

        expect:
        userRoleApiWrapper.api.getUserRoles(user.id, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_FORBIDDEN))
    }
}
