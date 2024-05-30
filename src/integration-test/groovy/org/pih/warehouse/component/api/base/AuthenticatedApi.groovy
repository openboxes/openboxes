package org.pih.warehouse.component.api.base

import io.restassured.specification.RequestSpecification

/**
 * Base class for all API classes that require an authenticated user.
 */
abstract class AuthenticatedApi extends Api {

    final AuthenticatedApiContext authenticatedApiContext

    AuthenticatedApi(AuthenticatedApiContext authenticatedApiContext) {
        this.authenticatedApiContext = authenticatedApiContext
    }

    RequestSpecification getBaseRequestSpec() {
        return authenticatedApiContext.baseRequestSpec
    }
}
