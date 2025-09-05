package org.pih.warehouse.core.date

/**
 * The different ways that we display dates
 */
enum DateDisplayFormat {

    /**
     * As used by our APIs.
     *
     * This enum value is likely to be unused since our APIs simply call toString() on dates but we add it here
     * for the sake of clarity.
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
