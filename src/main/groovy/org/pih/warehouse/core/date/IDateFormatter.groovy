package org.pih.warehouse.core.date

/**
 * A formatter that converts date objects to strings.
 */
interface IDateFormatter<T> {

    /**
     * Stringifies a given date object.
     */
    String format(T date)
}
