package org.pih.warehouse.component.api.base

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
abstract class ApiWrapper <T> {

    /**
     * The API class that we're wrapping.
     *
     * Note that we can't autowire a generic field like this because if we add a constructor, it tries to load in
     * the abstract Api class, which errors. This is fine, it just means we need to leave it to the child class to
     * provide the autowired constructor.
     */
    final T api

    ApiWrapper(T api) {
        this.api = api
    }
}
