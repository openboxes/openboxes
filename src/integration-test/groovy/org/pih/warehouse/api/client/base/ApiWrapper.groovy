package org.pih.warehouse.api.client.base

import org.pih.warehouse.api.util.JsonObjectUtil
import org.pih.warehouse.api.util.ResponseSpecUtil

/**
 * API wrappers enhance our base API classes, providing shortcuts for utilizing their endpoints in our
 * tests so that the API classes can stay small.
 *
 * Wrappers are meant to wrap only a single API class, and should not need to ever be called by other wrappers,
 * only from individual tests themselves. If you find the need to call a wrapper from another one, consider breaking
 * out the common logic to some test helper.
 *
 * Additionally, avoid adding methods to a wrapper that are too specific to any individual test scenario. These classes
 * are meant for helper methods that will be useful generally, across test specs.
 */
abstract class ApiWrapper<T> {

    final T api
    final ResponseSpecUtil responseSpecUtil
    final JsonObjectUtil jsonObjectUtil

    ApiWrapper(T api, ResponseSpecUtil responseSpecUtil, JsonObjectUtil jsonObjectUtil) {
        this.api = api
        this.responseSpecUtil = responseSpecUtil
        this.jsonObjectUtil = jsonObjectUtil
    }
}
