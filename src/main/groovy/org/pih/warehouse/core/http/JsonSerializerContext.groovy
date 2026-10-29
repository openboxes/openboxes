package org.pih.warehouse.core.http

import org.springframework.http.HttpStatus

/**
 * Holds the context required to build an HTTP Response with a JSON body.
 */
class JsonSerializerContext {

    /**
     * The HTTP status code to return.
     */
    int status = HttpStatus.OK.value()

    /**
     * A map of custom fields to include in the response object.
     *
     * These fields are in addition to the automatically populated fields (such as "data" and "status"),
     * and will be added at the root level of the response.
     *
     * For example, if given an additionalFields == [myField: 0], the response will contain:
     *
     * {
     *     data: [...],
     *     status: 200,
     *     myField: 0,
     * }
     */
    Map<String, Object> additionalFields = [:]
}
