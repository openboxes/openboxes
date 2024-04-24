package org.pih.warehouse.test.api.base

import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification

import static io.restassured.RestAssured.given

abstract class Api {
    /**
     * Contains the specification that should be common to all our authenticated APIs.
     * This saves us from having to define the full spec for each individual API call.
     */
    RequestSpecification defaultRequestSpec

    Api(RequestSpecification defaultRequestSpec) {
        this.defaultRequestSpec = defaultRequestSpec
    }

    /**
     * Issue a request to the server using the provide request and response specifications.
     */
    protected Response request(RequestSpecification requestSpec, ResponseSpecification responseSpec,
                               Method method, String uri) {
        RequestSpecification fullRequestSpec = mergeRequestSpecWithDefault(requestSpec)
        return given(fullRequestSpec, responseSpec)
                    .request(method, uri)
//                .then()
//                    .log().all()//ifStatusCodeIsEqualTo(404)//HttpStatus.SC_INTERNAL_SERVER_ERROR) // TODO: make this work!
//                .extract().response()
    }

    private RequestSpecification mergeRequestSpecWithDefault(RequestSpecification requestSpec) {
        return requestSpec == null ? defaultRequestSpec : new RequestSpecBuilder()
                .addRequestSpecification(defaultRequestSpec)
                .addRequestSpecification(requestSpec)
                .build()
    }
}
