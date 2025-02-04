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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
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

    /**
     * Formats for parsing a String to a java.util.Date. Mirror the formats defined in application.yml under the
     * grails.databinding.dateFormats setting. List order is important as formats will be attempted in order.
     */
    private static final List<SimpleDateFormat> DEFAULT_SIMPLE_DATE_FORMATS = [
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss XXX"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss XX"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss X"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm XXX"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm XX"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm X"),
            new SimpleDateFormat("MM/dd/yyyy"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXX"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXX"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mmX"),
    ]

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
     * Binds a String to a (deprecated) java.util.Date. If no format is given, will use the default configured formats.
     *
     * @Deprecated Only exists to support old endpoints that manually bind their data. New APIs should not use this
     *             approach, and should use Command Objects in controller method args to auto-bind the request body.
     */
    static Date asDate(String date, SimpleDateFormat format=null) {
        if (StringUtils.isBlank(date)) {
            return null
        }

        String dateSanitized = date.trim()
        if (format != null) {
            return format.parse(dateSanitized)
        }

        for (SimpleDateFormat defaultFormat : DEFAULT_SIMPLE_DATE_FORMATS) {
            try {
                return defaultFormat.parse(dateSanitized)
            } catch (ParseException ignored) {
                // Do nothing. Try the next format.
            }
        }
        throw new ParseException("Unparseable date: ${date}", 0)
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
