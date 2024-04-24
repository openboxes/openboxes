package org.pih.warehouse.test.api.base


import io.restassured.specification.RequestSpecification
import org.pih.warehouse.test.util.common.RandomUtil
import org.pih.warehouse.test.util.common.ResponseSpecUtil

/**
 * API service classes are convenience wrappers on APIs that provide shortcuts for utilizing those endpoints in your
 * tests. They're for the case where you need to use an API in your test but don't want to worry about testing it,
 * you just want it to work. Services promise that the APIs work correctly and have been tested elsewhere.
 *
 * Services are not to be used by other services, only from the tests themselves so that we can ensure the
 * cleanup method is being correctly invoked.
 *
 * They're also not to be used if you actually want to test the API. In that case just use the API class directly.
 */
abstract class ApiService {

    /**
     * Contains the specification that should be common to all our authenticated APIs.
     * This saves us from having to define the full spec for each individual API call.
     */
    RequestSpecification defaultRequestSpec

    RandomUtil randomUtil
    ResponseSpecUtil responseSpecUtil


    // TODO: move this to a helper so our tests can use them too!
    // We define some response specs here for the most common response codes for easy re-use.
//    final ResponseSpecification OK_RESPONSE_SPEC = buildStatusCodeResponseSpec(HttpStatus.SC_OK)
//    final ResponseSpecification CREATED_RESPONSE_SPEC = buildStatusCodeResponseSpec(HttpStatus.SC_CREATED)
//    final ResponseSpecification NOT_FOUND_RESPONSE_SPEC = buildStatusCodeResponseSpec(HttpStatus.SC_NOT_FOUND)

    ApiService(RequestSpecification defaultRequestSpec) {
        this.defaultRequestSpec = defaultRequestSpec

        randomUtil = new RandomUtil()
        responseSpecUtil = new ResponseSpecUtil()
    }

    /**
     * Clean up all test data that was created via the service.
     *
     * The easiest way to achieve this is to have the service class keep a list of all the object ids that are created
     * via its service methods, then the cleanup can iterate through them all, calling the delete API.
     */
    abstract void cleanup()

    /**
     * For use when we only care that an API returned some response code and don't need to do any actual asserts
     * on the response body itself (Ex: We POST some data and just want to ensure we get a non-error response back).
     */
//    static ResponseSpecification buildStatusCodeResponseSpec(int statusCode) {
//        return new ResponseSpecBuilder()
//                .expectStatusCode(statusCode)
//                .build()
//    }
}
