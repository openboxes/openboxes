package org.pih.warehouse.component.api.auth

import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject

import org.pih.warehouse.component.api.base.ApiWrapper

class AuthApiWrapper extends ApiWrapper {

    private AuthApi authApi

    AuthApiWrapper(RequestSpecification defaultRequestSpec) {
        super(defaultRequestSpec)

        authApi = new AuthApi(defaultRequestSpec)
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
        return authApi.login(body.toString(), responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }
}
