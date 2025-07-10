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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.databinding.DataBindingConstants

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
     * Formatters for displaying dates. These should only be used by GSPs and file exporters. Everything else should
     * return the date object itself to the frontend (which will get formatted to an ISO formatted string).
     *
     * We should strive to only ever use a single format per date type. For example, we should only have one format
     * for displaying day + month + year. This allows us to be consistent in how we display date fields in the app.
     */
    static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy")
    static final DateTimeFormatter DISPLAY_DATE_TIME_OFFSET_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm XXX")
    static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
    /**
     * Converts a LocalDate to a date-only string for display. This method should only be used by GSPs and file
     * exporters. Otherwise we should return the date object as is and let the frontend decide the display format.
     *
     * @return a formatted date String. Ex: "01/Jan/2025"
     */
    static String asDateForDisplay(LocalDate date) {
        return DISPLAY_DATE_FORMATTER.format(date)
    }

    /**
     * Converts a Date to a date-only string for display. This method should only be used by GSPs and file exporters.
     * Otherwise we should return the date object as is and let the frontend decide the display format.
     * Useful when working with old code that uses the old Date format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the system timezone.
     * @return a formatted date String. Ex: "01/Jan/2025"
     */
    static String asDateForDisplay(Date date, ZoneId zone=null) {
        return asDateForDisplay(asLocalDate(date, zone))
    }

    /**
     * Converts an Instant to a date + time + offset string for display. This method should only be used by GSPs and
     * file exporters. Otherwise we should return the date object as is and let the frontend decide the display format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the system timezone.
     * @return a formatted datetime String. Ex: "01/Jan/2025 00:00 +05:00"
     */
    static String asDateTimeForDisplay(Instant instant, ZoneId zone=null) {
        ZoneId zoneToUse = zone ?: getSystemZoneOffset()
        return asDateTimeForDisplay(instant.atZone(zoneToUse))
    }

    /**
     * Converts a ZonedDateTime to a date + time + offset string for display. This method should only be used by GSPs
     * and file exporters. Otherwise we should return the date object as is and let the frontend decide the display
     * format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the existing timezone
     *        of the ZonedDateTime.
     * @return a formatted datetime String. Ex: "01/Jan/2025 00:00 +05:00"
     */
    static String asDateTimeForDisplay(ZonedDateTime zonedDateTime, ZoneId zone=null) {
        // If we're not given a zone to display, we retain the zone as defined in the ZonedDateTime. We could have
        // converted it to the system timezone, but it felt safer to preserve the ZDT's state. In most cases we'll be
        // specifying the timezone, and when we don't, the ZDT will be in server time anyways, so it shouldn't matter.
        ZonedDateTime zonedDateTimeToUse = zone ? zonedDateTime.withZoneSameInstant(zone) : zonedDateTime
        return DISPLAY_DATE_TIME_OFFSET_FORMATTER.format(zonedDateTimeToUse)
    }

    /**
     * Converts a Date to a date + time + offset string for display. This method should only be used by GSPs and file
     * exporters. Otherwise we should return the date object as is and let the frontend decide the display format.
     * Useful when working with old code that uses the old Date format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the system timezone.
     * @return a formatted datetime String. Ex: "01/Jan/2025 00:00 +05:00"
     */
    static String asDateTimeForDisplay(Date date, ZoneId zone=null) {
        return asDateTimeForDisplay(asInstant(date), zone)
    }

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
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the system timezone.
     */
    static ZonedDateTime asZonedDateTime(Instant instant, ZoneId zone=null) {
        ZoneId zoneToUse = zone ?: getSystemZoneOffset()
        return instant ? instant.atZone(zoneToUse) : null
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to an ZonedDateTime. If no timezone is provided,
     * we assume UTC. Useful when working with old code that uses the old format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the system timezone.
     */
    static ZonedDateTime asZonedDateTime(Date date, ZoneId zone=null) {
        ZoneId zoneToUse = zone ?: getSystemZoneOffset()
        return date ? asInstant(date).atZone(zoneToUse) : null
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to a LocalDate.
     * Useful when working with old code that uses the old format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the system timezone.
     */
    static LocalDate asLocalDate(Date date, ZoneId zone=null) {
        ZoneId zoneToUse = zone ?: getSystemZoneOffset()
        return date ? asInstant(date).atZone(zoneToUse).toLocalDate() : null
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

        // If we're given a specific format, use that to parse the string directly to a Date. We shouldn't ever need
        // to define this unless we know we're going to be given input in a non-conventional format. The flexible
        // format should cover all the normal cases.
        if (format != null) {
            return format.parse(dateSanitized)
        }

        // Otherwise, try to parse using our flexible list of supported formats. We parse to an Instant and then convert
        // to a Date since it allows us to use DateTimeFormatter, which is less error-prone than SimpleDateFormat.
        Instant instant = Instant.from(DataBindingConstants.FLEXIBLE_DATE_TIME_ZONE_FORMAT
                // We calculate the timezone offset dynamically instead of putting it directly into the formatter
                // in the rare case that the server is configured in a system timezone that is sensitive to daylight
                // savings time (ex: America/Vancouver is PST (-08:00) from November-March and PDT (-07:00) otherwise).
                // This way, we can gracefully handle DST rollovers.
                // TODO: A bug in JDK 8 prevents us from being able to do this. Once we upgrade to Java 9+, we can
                //       re-enable this line and remove the "parseDefaulting" in the FLEXIBLE_DATE_TIME_ZONE_FORMAT
                //       https://stackoverflow.com/questions/41999421/how-does-datetimeformatters-override-zone-work-when-parsing
                //.withZone(getSystemZoneOffset())
                .parse(dateSanitized))
        return Date.from(instant)
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
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example. Defaults to the system timezone.
     */
    static Date asDate(LocalDate localDate, ZoneId zone=null) {
        ZoneId zoneToUse = zone ?: getSystemZoneOffset()
        return localDate ? asDate(localDate.atStartOfDay(zoneToUse)) : null
    }

    /**
     * Fetches the ZoneOffset of the system at a given point in time. If no instant is provided, will return
     * the current timezone offset of the system.
     */
    static ZoneOffset getSystemZoneOffset(Instant instant=null) {
        // Extracts the offset rules for the timezone (ie what the offset is for a zone for a given time of year),
        // then uses those rules to determine the offset (ex: '-05:00') for the given instant. This check is required
        // because a zone can be in one of many offsets depending on the time of year due to daylight savings.
        return ZoneId.systemDefault().getRules().getOffset(instant ?: Instant.now())
    }

    /**
     * Converts a Date to a time-only string for display.
     */
    static String asTimeForDisplay(Date date) {
        if (!date) {
            return null
        }
        LocalTime time = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime()

        return DISPLAY_TIME_FORMATTER.format(time)
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
