package org.pih.warehouse.api.util

import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.TestComponent

@TestComponent
class ResponseSpecUtil {

    /**
     * We define some response specs here for the most common response codes for easy re-use.
     */
    final ResponseSpecification OK_RESPONSE_SPEC = buildStatusCodeResponseSpec(HttpStatus.SC_OK)                  // 200
    final ResponseSpecification CREATED_RESPONSE_SPEC = buildStatusCodeResponseSpec(HttpStatus.SC_CREATED)        // 201
    final ResponseSpecification NO_CONTENT_RESPONSE_SPEC = buildStatusCodeResponseSpec(HttpStatus.SC_NO_CONTENT)  // 204
    final ResponseSpecification NOT_FOUND_RESPONSE_SPEC = buildStatusCodeResponseSpec(HttpStatus.SC_NOT_FOUND)    // 404

    /**
     * For use when we only care that an API returned some response code and don't need to do any actual asserts
     * on the response body itself (Ex: We POST some data and just want to ensure we get a non-error response back).
     */
    ResponseSpecification buildStatusCodeResponseSpec(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .build()
    }
}
