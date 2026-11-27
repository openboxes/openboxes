package org.pih.warehouse.core.date

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.DateUtil

/**
 * Deserializes input objects into Dates.
 *
 * For use only by data importers. APIs should always use command objects which automatically handle data binding.
 *
 * Designed to be temporary. We aim to eventually fully switch to java.time classes, which would deprecate this class.
 */
@Component
class JavaUtilDateParser extends AbstractDateParser<Date> {

    private static final String MISSING_DATE_FIELD_TYPE_MESSAGE = "java.util.Date is used for both date-only and " +
            "datetime fields, so a dateFieldType must be specified when parsing to a Date."

    /**
     * Intentionally not the default parser for Date. Date is used for both date-only and datetime fields, so callers
     * must call the parser manually, explicitly declaring which type it is (see {@link DateFieldType}).
     */
    @Override
    boolean isDefaultParserForType() {
        return false
    }

    @Override
    protected Date parseImpl(Object date, DateParserContext<Date> context) {
        // If the source object's type is unambiguous, parse straight from it.
        switch(date) {
            case LocalDate:
                return asDate(date as LocalDate)
            case Instant:
                return asDate(date as Instant)
            case ZonedDateTime:
                return asDate(date as ZonedDateTime)
            default:
                // Otherwise the type is ambiguous so we need to rely on dateFieldType.
                break
        }

        switch (context?.dateFieldType) {
            case DateFieldType.LOCAL_DATE:
                return parseAsLocalDate(date, context)
            case DateFieldType.INSTANT:
                return parseAsInstant(date, context)
            default:
                throw new IllegalArgumentException(MISSING_DATE_FIELD_TYPE_MESSAGE)
        }
    }

    /**
     * Parses a date-only field (see {@link DateFieldType#LOCAL_DATE}). Date-only fields are stored as midnight in the
     * system timezone (which is also how JavaUtilDateFormatter displays them), so we parse to a LocalDate, then convert
     * it to a Date in that timezone. Supports date-only Strings (ex: "2000-01-31") and Excel formatted dates.
     */
    private Date parseAsLocalDate(Object date, DateParserContext<Date> context) {
        switch (date) {
            case String:
                return asDate(LocalDateParser.asLocalDate(date as String))
            case double:
            case Double:
            case BigDecimal:
                return asDate(LocalDateParser.asLocalDate(date as double, context))
            default:
                throw new UnsupportedOperationException("Cannot parse date of type [${date.class}]")
        }
    }

    /**
     * Parses a datetime field (see {@link DateFieldType#INSTANT}). We parse it the same way as an Instant, which means
     * that Excel formatted dates, date-only Strings and (if the context allows them) datetime Strings without a
     * timezone are in the user's timezone. This mirrors how BulkDataWriter exports java.util.Date datetime fields.
     */
    private Date parseAsInstant(Object date, DateParserContext<Date> context) {
        switch (date) {
            case String:
                return asDate(InstantParser.asInstant(date as String, currentTimezone, context as DateParserContext))
            case double:
            case Double:
            case BigDecimal:
                return asDate(InstantParser.asInstant(date as double, currentTimezone, context as DateParserContext))
            default:
                throw new UnsupportedOperationException("Cannot parse date of type [${date.class}]")
        }
    }

    /**
     * Binds a String to a (deprecated) java.util.Date.
     *
     * Unlike the parse method, this method is static, and so has no access to the requesting user's timezone. As such:
     * - LOCAL_DATE: Expects a date-only String (ex: "2000-01-31"). Results in midnight in the system timezone.
     * - INSTANT: Expects a datetime String that includes the timezone (ex: "2000-01-31T13:00:00-05:00"). Strings
     *            without a timezone (including date-only Strings) are rejected because we have no timezone to assume.
     *
     * @Deprecated Only exists to support old endpoints that manually bind their data. New APIs should not use this
     *             approach, and should use Command Objects in controller method args to auto-bind the request body.
     */
    static Date asDate(String date, DateFieldType dateFieldType) {
        if (StringUtils.isBlank(date)) {
            return null
        }

        switch (dateFieldType) {
            case DateFieldType.LOCAL_DATE:
                return asDate(LocalDateParser.asLocalDate(date))
            case DateFieldType.INSTANT:
                // No default timezone, so the String must specify one.
                return asDate(InstantParser.asInstant(date))
            default:
                throw new IllegalArgumentException(MISSING_DATE_FIELD_TYPE_MESSAGE)
        }
    }

    /**
     * Null-safe conversion of an Instant to a (deprecated) java.util.Date.
     * Useful when working with old code that uses the old format.
     */
    static Date asDate(Instant instant) {
        return instant ? Date.from(instant) : null
    }

    /**
     * Null-safe conversion of a ZonedDateTime to a (deprecated) java.util.Date.
     * Useful when working with old code that uses the old format.
     */
    static Date asDate(ZonedDateTime zonedDateTime) {
        return zonedDateTime ? asDate(zonedDateTime.toInstant()) : null
    }

    /**
     * Null-safe conversion of a LocalDate to a (deprecated) java.util.Date.
     * Useful when working with old code that uses the old format.
     *
     * NOTE: For backwards compatability purposes, the date will always be in the system timezone.
     */
    static Date asDate(LocalDate localDate) {
        /*
         * In the rare case where your server is in a timezone that is affected by daylight savings (don't do this,
         * the server timezone should be UTC), we need to be careful about this conversion because the given date
         * might have a different offset than the current offset.
         *
         * For example: The "America/Vancouver" timezone is PST (-08:00) from Nov-Mar and PDT (-07:00) from Mar-Nov
         * while DST is in effect. So if it's currently Jan 1st, your offset is -8, but if the given date is May 1st,
         * the offset at the time would be -7.
         *
         * When converting to a Date, this is taken into account, so you have a datetime at midnight in the offset
         * of the zone as it was on that date. Using the above example, that would be May 1st at midnight in UTC-07:00.
         * This is noteworthy because it is a different instant than if it used your current offset, which would be
         * May 1st at midnight in UTC-08:00 (so an hour difference).
         */
        return localDate ? asDate(localDate.atStartOfDay(DateUtil.getSystemZoneId())) : null
    }
}
