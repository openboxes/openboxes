package org.pih.warehouse.test.base

import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Cookie
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.pih.warehouse.test.util.common.JsonPathUtil
import org.pih.warehouse.test.util.common.RandomUtil
import org.pih.warehouse.test.util.common.ResponseSpecUtil
import spock.lang.Shared
import spock.lang.Specification

import org.pih.warehouse.test.api.auth.AuthApiService
import org.pih.warehouse.test.api.base.ApiService

/**
 * Base class for all of our API tests.
 */
abstract class ApiSpec extends Specification {

    @Shared
    String baseURI = System.getProperty("server.host")?:"http://localhost"

    @Shared
    String basePath = System.getProperty("server.path")?:"/openboxes"

    @Shared
    int port = System.getProperty("server.port")?Integer.valueOf(System.getProperty("server.port")):8080

    @Shared
    String username = System.getProperty("username")?:"admin"

    @Shared
    String password = System.getProperty("password")?:"password"

    @Shared
    Cookie cookie

    @Shared
    RequestSpecification baseRequestSpec

    @Shared
    RequestSpecification baseUnauthenticatedRequestSpec

    @Shared
    AuthApiService authApiService

    @Shared
    RandomUtil randomUtil

    @Shared
    ResponseSpecUtil responseSpecUtil

    @Shared
    JsonPathUtil jsonPathUtil

    private List<ApiService> services

    /**
     * List all instances of the API services that the test uses so that we can ensure we're always calling their
     * cleanup methods on test teardown.
     */
    abstract List<ApiService> registerApiServices()

    void setupSpec() {
        RestAssured.basePath = basePath
        RestAssured.baseURI = baseURI
        RestAssured.port = port

        baseUnauthenticatedRequestSpec = getDefaultUnauthenticatedRequestSpec()

        authApiService = new AuthApiService(baseUnauthenticatedRequestSpec)
        randomUtil = new RandomUtil()
        responseSpecUtil = new ResponseSpecUtil()
        jsonPathUtil = new JsonPathUtil()

        setupTestUser()

        baseRequestSpec = getDefaultRequestSpec()
    }

    void setup() {
        services = registerApiServices() ?: []
        services.add(authApiService)
    }

    void cleanup() {
        services.forEach { service ->
            service.cleanup()
        }
    }

    /**
     * Authenticate a user for use by all tests.
     */
    void setupTestUser() {
        Response authResponse = authApiService.loginOK(username, password)
        cookie = authResponse.getDetailedCookie("JSESSIONID")
    }

    /**
     * Builds a default RequestSpecification for use by all authenticated APIs in the test. This way we don't need
     * to re-specify all these details for each individual API request. Using this, all a request needs to add are
     * its request specific fields such as request params and query params.
     */
    RequestSpecification getDefaultRequestSpec() {
        return new RequestSpecBuilder()
                .addCookie(cookie)
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .build()
    }

    /**
     * Builds a default RequestSpecification for use by all unauthenticated APIs in the test. This way we don't need
     * to re-specify all these details for each individual API request. Using this, all a request needs to add are
     * its request specific fields such as request params and query params.
     */
    RequestSpecification getDefaultUnauthenticatedRequestSpec() {
        return new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .build()
    }
}
