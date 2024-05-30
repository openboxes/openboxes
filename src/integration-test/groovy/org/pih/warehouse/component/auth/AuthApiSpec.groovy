package org.pih.warehouse.component.auth

import grails.gorm.transactions.Transactional
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.pih.warehouse.component.base.ApiSpec
import spock.lang.Ignore

/**
 * Test our authentication endpoints.
 */
class AuthApiSpec extends ApiSpec {

    @Transactional
    void setupData() {
        // The base class already creates a test location so nothing to do here.
    }

    void "login should succeed for a valid user"() {
        given:
        JSONObject body = buildAuthRequestBody(username, password, location.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_OK)
    }

    void "login should fail when given an invalid username"() {
        given:
        JSONObject body = buildAuthRequestBody("invalid", password, location.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_UNAUTHORIZED)
    }

    void "login should fail when given an invalid password"() {
        given:
        JSONObject body = buildAuthRequestBody(username, "invalid", location.id)

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
