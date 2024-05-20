package org.pih.warehouse.component.api.auth

import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.component.api.base.ApiWrapper
import org.pih.warehouse.component.util.ResponseSpecUtil

@TestComponent
class AuthApiWrapper extends ApiWrapper<AuthApi> {

    final ResponseSpecUtil responseSpecUtil

    AuthApiWrapper(AuthApi api, ResponseSpecUtil responseSpecUtil) {
        super(api)
        this.responseSpecUtil = responseSpecUtil
    }

    /**
     * Expects to successfully authenticate into the app with the provided user.
     */
    Response loginOK(String username, String password, String locationId) {
        JSONObject body = new JSONObject()
                .put("username", username)
                .put("password", password)
                .put("location", locationId)

        return login(body, HttpStatus.SC_OK)
    }

    /**
     * Expects the given status code is returned when authenticating into the app with the provided user.
     */
    Response login(JSONObject body, int statusCode) {
        return api.login(body.toString(), responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }
}
