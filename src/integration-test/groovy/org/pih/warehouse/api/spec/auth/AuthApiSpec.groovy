package org.pih.warehouse.api.spec.auth

import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.pih.warehouse.api.spec.base.ApiSpec
import spock.lang.Ignore

/**
 * Test our authentication endpoints.
 */
class AuthApiSpec extends ApiSpec {

    void "login should succeed for a valid user"() {
        given:
        JSONObject body = buildAuthRequestBody(username, password, facility.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_OK)
    }

    void "login should fail when given an invalid username"() {
        given:
        JSONObject body = buildAuthRequestBody("invalid", password, facility.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_UNAUTHORIZED)
    }

    void "login should fail when given an invalid password"() {
        given:
        JSONObject body = buildAuthRequestBody(username, "invalid", facility.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_UNAUTHORIZED)
    }

    @Ignore("This actually succeeds, even though the location doesn't exist. Re-enable this test when that's fixed.")
    void "login should fail when given an invalid location"() {
        given:
        JSONObject body = buildAuthRequestBody(username, password, INVALID_ID)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_UNAUTHORIZED)
    }

    JSONObject buildAuthRequestBody(String username, String password, String location) {
        return new JSONObject()
                .put("username", username)
                .put("password", password)
                .put("location", location)
    }
}
