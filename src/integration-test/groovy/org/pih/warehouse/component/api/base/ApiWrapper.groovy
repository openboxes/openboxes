package org.pih.warehouse.component.api.base

import io.restassured.specification.RequestSpecification

import org.pih.warehouse.util.common.RandomUtil
import org.pih.warehouse.util.common.ResponseSpecUtil

/**
 * API wrappers enhance our base API classes, providing shortcuts for utilizing their endpoints in our
 * tests so that the API classes can stay small.
 *
 * Wrappers are meant to wrap only a single API class, or a group of very tightly coupled API classes, and so should
 * not need to ever be called by other wrappers, only from individual tests themselves. If you find the need to call
 * a wrapper from another one, consider breaking out the common logic to some test helper.
 *
 * Additionally, avoid putting helper methods that are too specific to any individual test scenario. These classes
 * are meant for helper methods that will be useful generally, across test specs.
 */
abstract class ApiWrapper {

    /**
     * Contains the specification that should be common to all our authenticated APIs.
     * This saves us from having to define the full spec for each individual API call.
     */
    RequestSpecification defaultRequestSpec

    RandomUtil randomUtil
    ResponseSpecUtil responseSpecUtil

    ApiWrapper(RequestSpecification defaultRequestSpec) {
        this.defaultRequestSpec = defaultRequestSpec

        randomUtil = new RandomUtil()
        responseSpecUtil = new ResponseSpecUtil()
    }
}
