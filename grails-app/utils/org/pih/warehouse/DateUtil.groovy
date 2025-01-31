/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse

import grails.validation.ValidationException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import org.apache.commons.lang.StringUtils

/**
 * Utility methods on date and datetime objects.
 *
 * In our system, we try to work with a small subset of the datetime-related classes:
 *
 * - java.time.Instant: An absolute moment in time (measured in seconds since UTC epoch). We should work with Instant
 *                      whenever possible since it is specific enough to be convertible to other formats, while
 *                      still being timezone agnostic (and so avoids any locale conversion and weirdness).
 *
 * - java.time.LocalDate: A date (day + month + year) with no time or timezone component. Useful for setting fixed
 *                        dates, where switching to a different timezone should never change the date (ex: expiry date).
 *
 * - java.time.ZonedDateTime: A datetime (via LocalDateTime) in a timezone (via ZoneOffset).
 *                            Useful for working with a human readable datetime in a specific locale, where switching
 *                            to a different timezone should change the resulting value. Only to be used if we have
 *                            user-locale-specific logic (ex: We need to check if some datetime would be the next
 *                            day for the requesting user).
 *
 * We also have java.util.Date, which is deprecated and should not be used going forward.
 */
class DateUtil {

    /*
     * Patterns that follow the ISO-8601 + RPC 3339 date formats while allowing for some slight variation in
     * the pattern.
     *
     * Ex: "2000-01-01", "2000/01/01", "20000101" are all valid strings according to FLEXIBLE_DATE_PATTERN.
     * Ex: both "2000-01-01" and "2000-01-01T00:00:00Z" are valid strings according to FLEXIBLE_DATE_TIME_ZONE_PATTERN.
     *
     * We do this mainly to support a wider range of user INPUT, making our parsing logic more flexible.
     * Internally, we should strive to use a consistent structure and to always OUTPUT the same format (see below
     * "Fixed Date Formats").
     *
     * Note that more specific optionals must be defined in front of less specific ones (ex: [HH:mm:ss][HH:mm],
     * not [HH:mm][HH:mm:ss]).
     *
     * Also note that depending on what date type you're parsing into, this can trigger an error. For example, Instant
     * requires time and timezone information, and so FLEXIBLE_DATE_TIME_ZONE_PATTERN will fail to parse "2000-01-01"
     * unless the associated DateTimeFormatter provides a default (via a "withZone(ZoneId)" call).
     */
    private static final String FLEXIBLE_DATE_PATTERN = "[yyyy-MM-dd][yyyy/MM/dd][yyyy MM dd][yyyyMMdd]"
    private static final String FLEXIBLE_TIME_PATTERN =
            "[HH:mm:ss.SSS][HH:mm:ss:SSS][HHmmssSSS]" +
            "[HH:mm:ss.SS][HH:mm:ss:SS][HHmmssSS]" +
            "[HH:mm:ss.S][HH:mm:ss:S][HHmmssS]" +
            "[HH:mm:ss][HHmmss]" +
            "[HH:mm][HHmm]"
    private static final String FLEXIBLE_DATE_TIME_ZONE_PATTERN =
            "${FLEXIBLE_DATE_PATTERN}[['T'][ ]${FLEXIBLE_TIME_PATTERN}][[XXX][XX][X]]"

    /*
     * Flexible Date Formats
     *
     * "Flexible" in that they allow for multiple formats, and provide appropriate defaults if fields are not provided.
     *
     * We should use these flexible formats only when binding user-input Strings to certain data types.
     * To convert date types back to Strings, just call toString() on them. The reasoning is that we want to accept as
     * wide a range of user inputs as possible, but internally we want to work with and return a single, known format.
     */
    static final DateTimeFormatter FLEXIBLE_DATE_TIME_ZONE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern(FLEXIBLE_DATE_TIME_ZONE_PATTERN)
            // Defaults to 00:00:00.000 if time is not provided in the String
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            // Defaults to UTC if timezone is not provided in the String
            .parseDefaulting(ChronoField.OFFSET_SECONDS, ZoneOffset.UTC.getTotalSeconds())
            .toFormatter()
    static final DateTimeFormatter FLEXIBLE_DATE_FORMAT = DateTimeFormatter.ofPattern(FLEXIBLE_DATE_PATTERN)
    static final DateTimeFormatter FLEXIBLE_TIME_FORMAT = DateTimeFormatter.ofPattern(FLEXIBLE_TIME_PATTERN)

    /**
     * To be used when parsing a String to a java.util.Date.
     *
     * @Deprecated SimpleDateFormat is functionally deprecated as of Java 8 and is replaced by DateTimeFormatter.
     *             Don't use SimpleDateFormat anywhere else from now on.
     */
    private static final SimpleDateFormat FLEXIBLE_SIMPLE_FORMAT = new SimpleDateFormat(FLEXIBLE_DATE_TIME_ZONE_PATTERN)

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to an Instant.
     * Useful when working with old code that uses the old format.
     */
    static Instant asInstant(Date date) {
        return date ? date.toInstant() : null
    }

    /**
     * Null-safe conversion of a ZonedDateTime to an Instant.
     */
    static Instant asInstant(ZonedDateTime zonedDateTime) {
        return zonedDateTime ? zonedDateTime.toInstant() : null
    }

    /**
     * Null-safe conversion of an Instant to a ZonedDateTime. If no timezone is provided, we assume UTC.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static ZonedDateTime asZonedDateTime(Instant instant, ZoneId zone=ZoneOffset.UTC) {
        return instant ? instant.atZone(zone) : null
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to an ZonedDateTime. If no timezone is provided,
     * we assume UTC. Useful when working with old code that uses the old format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static ZonedDateTime asZonedDateTime(Date date, ZoneId zone=ZoneOffset.UTC) {
        return date ? asInstant(date).atZone(zone) : null
    }

    /**
     * Binds a String to a (deprecated) java.util.Date.
     *
     * @Deprecated Only exists to support old endpoints that manually bind their data. New APIs should not use this
     *             approach, and should use Command Objects in controller method args to auto-bind the request body.
     */
    static Date asDate(String date, SimpleDateFormat format=FLEXIBLE_SIMPLE_FORMAT) {
        return StringUtils.isBlank(date) ? null : format.parse(date.trim())
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

    static Date clearTime(Date date) {
        Calendar calendar = Calendar.getInstance()
        if (date) {
            calendar.setTime(date)
            // Set time fields to zero
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Put it back in the Date object
            date = calendar.getTime()
        }
        return date
    }


    static Date[] parseDateRange(String dateRangeStr, String format, String separator) {

        if (!dateRangeStr)
            return null

        String[] dateRanges = dateRangeStr.split(separator)
        if (dateRanges.length != 2) {
            throw new ValidationException("Date range must have exactly two dates")
        }

        Date[] dateRange = new Date[2]
        def dateFormat = new SimpleDateFormat(format)
        dateRanges.eachWithIndex { String dateRangeEntry, int i ->
            dateRange[i] = dateFormat.parse(dateRangeEntry)
        }
        return dateRange
    }

    static Map getDateRange(Date date, Integer relativeMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date)
        calendar.add(Calendar.MONTH, relativeMonth);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        Date firstDayOfMonth = calendar.time
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        Date lastDayOfMonth = calendar.time
        [startDate: firstDayOfMonth, endDate: lastDayOfMonth]
    }
}
