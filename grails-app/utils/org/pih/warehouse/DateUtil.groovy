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

/**
 * Utility methods on date and datetime objects.
 */
class DateUtil {

    /**
     * A Date representing the epoch instant, which is January 1, 1970, 00:00:00 GMT.
     */
    static final Date EPOCH_DATE = new Date(0)

    /**
     * Fetches the ZoneOffset of the system at a given point in time. If no instant is provided, will return
     * the current timezone offset of the system.
     */
    static ZoneOffset getSystemZoneOffset(Instant instant=null) {
        return getZoneOffset(getSystemZoneId(), instant)
    }

    /**
     * Fetches the ZoneOffset of the given zone at a given point in time. If no instant is provided, will return
     * the current timezone offset of the zone.
     */
    static ZoneOffset getZoneOffset(ZoneId zoneId, Instant instant=null) {
        // Extracts the offset rules for the timezone (ie what the offset is for a zone for a given time of year),
        // then uses those rules to determine the offset (ex: '-05:00') for the given instant. This check is required
        // because a zone can be in one of many offsets depending on the time of year due to daylight savings.
        return zoneId.getRules().getOffset(instant ?: Instant.now())
    }

    /**
     * Get the current ZoneId of the system.
     */
    static ZoneId getSystemZoneId() {
        return ZoneId.systemDefault()
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
