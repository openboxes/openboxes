package org.pih.warehouse.core.date

/**
 * What a java.util.Date field actually represents.
 *
 * java.util.Date (which we are in the process of replacing with java.time) is used for
 * both date-only and datetime fields, so any logic that relies on the Date needs to know
 * which type it is.
 *
 * Values in this enum are named after their equivalent java.time type. Fields should be
 * treated exactly as if they were that corresponding java.time type.
 */
enum DateFieldType {

    /**
     * A date-only field (no time or zone), equivalent to a {@link java.time.LocalDate}.
     *
     * Displayed as midnight in the SERVER's timezone. This helps the date be timezone agnostic.
     */
    LOCAL_DATE,

    /**
     * A datetime field (a point in time), equivalent to an {@link java.time.Instant}.
     *
     * Displayed in the USER's timezone.
     */
    INSTANT,
}
