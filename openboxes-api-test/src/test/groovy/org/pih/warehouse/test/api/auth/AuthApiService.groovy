package org.pih.warehouse.test.api.auth

import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.json.JSONObject
import org.pih.warehouse.test.api.base.ApiService

class AuthApiService extends ApiService {

    AuthApi authApi

    AuthApiService(RequestSpecification defaultRequestSpec) {
        super(defaultRequestSpec)

        authApi = new AuthApi(defaultRequestSpec)
    }

    void cleanup() {
        // We use pre-created users in our tests so nothing to clean up.
    }

    Response loginOK(String username, String password) {
        String body = new JSONObject()
                .put("username", username)
                .put("password", password)
                .put("location", 1)  // TODO: Create a test location for use exclusively by tests
                .toString()

        return authApi.login(body, responseSpecUtil.OK_RESPONSE_SPEC)
    }
}