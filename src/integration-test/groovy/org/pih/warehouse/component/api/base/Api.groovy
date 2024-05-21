package org.pih.warehouse.component.api.base

import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification

import static io.restassured.RestAssured.given

/**
 * For issuing Restful API calls to the server from our tests.
 */
abstract class Api {

    /**
     * Contains the base specification that should be common to all our API calls.
     * This saves us from having to define the full spec for each individual API call.
     */
    RequestSpecification defaultRequestSpec

    Api(RequestSpecification defaultRequestSpec) {
        this.defaultRequestSpec = defaultRequestSpec
    }

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
        return requestSpec == null ? defaultRequestSpec : new RequestSpecBuilder()
                .addRequestSpecification(defaultRequestSpec)
                .addRequestSpecification(requestSpec)  // Add the custom spec second so it overrides the default.
                .build()
    }
}
