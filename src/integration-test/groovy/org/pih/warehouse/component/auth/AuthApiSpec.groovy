package org.pih.warehouse.component.auth

import grails.gorm.transactions.Transactional
import org.apache.http.HttpStatus
import org.json.JSONObject
import org.pih.warehouse.component.api.auth.AuthApiWrapper
import org.pih.warehouse.component.base.ApiSpec
import spock.lang.Ignore
import spock.lang.Shared

class AuthApiSpec extends ApiSpec {

    @Shared
    AuthApiWrapper authApiWrapper

    @Transactional
    void setupData() {
        // The base class already creates a test location so nothing to do here.
    }

    void "login successful"() {
        given:
        JSONObject body = buildAuthRequestBody(username, password, location.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_OK)
    }

    void "login fails with invalid username"() {
        given:
        JSONObject body = buildAuthRequestBody("invalid", password, location.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_UNAUTHORIZED)
    }

    void "login fails with invalid password"() {
        given:
        JSONObject body = buildAuthRequestBody(username, "invalid", location.id)

        expect:
        authApiWrapper.login(body, HttpStatus.SC_UNAUTHORIZED)
    }

    @Ignore("This actually succeeds, even though the location doesn't exist. Re-enable this test when that's fixed.")
    void "login fails with invalid location"() {
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
