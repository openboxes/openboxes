package org.pih.warehouse.core.date

/**
 * The different data formats that we display dates in.
 */
enum DateDisplayFormat {

    /**
     * As used by our APIs.
     *
     * This enum value is likely to be unused since dates are automatically bound to JSON in our APIs, but we add
     * it here for the sake of clarity.
     */
    JSON,

    /**
     * The old frontend pages that still use GSPs.
     */
    GSP,

    /**
     * As used by file exporters.
     */
    CSV,
}
