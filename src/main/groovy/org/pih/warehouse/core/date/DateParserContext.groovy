package org.pih.warehouse.core.date

import org.pih.warehouse.core.parser.ParserContext

/**
 * Context object containing the configuration fields for parsing in dates.
 * For a majority of cases the default settings can be used and so this context object will not be required.
 */
class DateParserContext<T> extends ParserContext<T> {

    /**
     * The epoch date to use when parsing to the given date type.
     *
     * This is required for parsing dates coming from Excel files and can likely be ignored for all other scenarios.
     */
    EpochDate epochDate = EpochDate.UNIX_EPOCH

    /**
     * True if datetime Strings that don't specify a timezone (ex: "2000-01-31 13:00:00") should be parsed in the
     * requesting user's timezone. Only used when parsing to a datetime (Instant, ZonedDateTime, or a java.util.Date
     * with a dateFieldType of INSTANT).
     *
     * By default, such Strings are rejected because APIs require the timezone to avoid ambiguity. Bulk data imports
     * enable this because Excel can't store timezones, so our exports (and files edited in Excel) don't include them.
     */
    boolean allowDateTimesWithoutTimezone = false

    /**
     * What a java.util.Date field represents. Only used by {@link JavaUtilDateParser}, where it is required.
     */
    DateFieldType dateFieldType
}
