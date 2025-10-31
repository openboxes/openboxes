package org.pih.warehouse.api.client.base

import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification

import static io.restassured.RestAssured.given

/**
 * For issuing Restful API calls to the server from our tests.
 * Avoid using API classes directly in your tests. If a wrapper class exists for the API, use that instead.
 */
abstract class Api {

    /**
     * The default request specification to apply to all API calls. If a field is not specified in the
     * request spec when making an API call, the value in the base spec is used.
     */
    abstract RequestSpecification getBaseRequestSpec()

    /**
     * Issue a Restful API request to the server using the provided request and response specifications.
     */
    protected Response request(RequestSpecification requestSpec, ResponseSpecification responseSpec,
                               Method method, String uri) {
        RequestSpecification mergedRequestSpec = mergeRequestSpecWithDefault(requestSpec)
        return given(mergedRequestSpec)
                .when()
                    .request(method, uri)
                .then()
                    .log().ifValidationFails()  // For easy debugging.
                    .spec(responseSpec)
                    .extract().response()
    }

    private RequestSpecification mergeRequestSpecWithDefault(RequestSpecification requestSpec) {
        RequestSpecification baseRequestSpec = getBaseRequestSpec()
        return requestSpec == null ? baseRequestSpec : new RequestSpecBuilder()
                .addRequestSpecification(baseRequestSpec)
                .addRequestSpecification(requestSpec)  // Add the custom spec second so it overrides the default.
                .build()
    }
}
