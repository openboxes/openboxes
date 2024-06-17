package org.pih.warehouse.api.client.base

import io.restassured.specification.RequestSpecification

/**
 * Base class for all API classes that do not require an authenticated user.
 */
abstract class UnauthenticatedApi extends Api {

    final UnauthenticatedApiContext unauthenticatedApiContext

    UnauthenticatedApi(UnauthenticatedApiContext unauthenticatedApiContext) {
        this.unauthenticatedApiContext = unauthenticatedApiContext
    }

    RequestSpecification getBaseRequestSpec() {
        return unauthenticatedApiContext.baseRequestSpec
    }
}
