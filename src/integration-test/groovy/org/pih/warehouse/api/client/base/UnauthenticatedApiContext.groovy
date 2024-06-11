package org.pih.warehouse.api.client.base

import io.restassured.specification.RequestSpecification
import org.springframework.boot.test.context.TestComponent

/**
 * Context Component containing all the data common to all unauthenticated APIs.
 */
@TestComponent
class UnauthenticatedApiContext {

    /**
     * Contains the base specification that should be common to all unauthenticated API calls.
     * This saves us from having to define the full spec for each individual API call.
     */
    RequestSpecification baseRequestSpec

    /**
     * Because some fields of the context are dynamically determined, we don't populate the context
     * at construction time. Instead it is loaded externally by the tests themselves.
     */
    void loadContext(RequestSpecification baseRequestSpec) {
        this.baseRequestSpec = baseRequestSpec
    }
}
