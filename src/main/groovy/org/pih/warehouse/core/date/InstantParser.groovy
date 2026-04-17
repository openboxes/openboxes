package org.pih.warehouse.core.date

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
import org.apache.poi.ss.usermodel.DateUtil
import org.springframework.stereotype.Component

import org.pih.warehouse.databinding.DataBindingConstants

/**
 * Deserializes input objects into Instants.
 *
 * For use only by data importers. APIs should always use command objects which automatically handle data binding.
 */
@Component
class InstantParser extends AbstractDateParser<Instant> {

    @Override
    protected Instant parseImpl(Object date, DateParserContext<Instant> context) {
        switch (date) {
            case String:
                return asInstant(date as String, currentTimezone, context)
            case double:
            case Double:
            case BigDecimal:
                return asInstant(date as double, currentTimezone, context)
            case ZonedDateTime:
                return asInstant(date as ZonedDateTime)
            case LocalDate:
                return asInstant(date as LocalDate, currentTimezone)
            case Date:
                return asInstant(date as Date)
            case Calendar:
                return asInstant(date as Calendar)
            default:
                throw new UnsupportedOperationException("Cannot parse date of type [${date.class}]")
        }
    }

    /**
     * Null-safe conversion of a Double to an Instant.
     * This is how Excel represents date fields (aka serial numbers) and so is useful when importing xlsx files.
     *
     * @param excelDate An Excel formatted double representing a date
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static Instant asInstant(Double excelDate, ZoneId zone, DateParserContext<Instant> context) {
        if (excelDate == null) {
            return null
        }
        if (!zone) {
            throw new DateTimeException('Cannot convert an Excel formatted double to an Instant without specifying a timezone')
        }

        /*
         * Dates can be represented as doubles when they are coming from Excel, so we let Apache POI handle converting
         * the date for us, rounding the date to the nearest second.
         *
         * It's important to note that the date coming from Excel is a datetime with no timezone, which maps most
         * closely to java.time.LocalDateTime. As such, to convert to an Instant we need the timezone to be specified.
         *
         * The integer portion of the double represents the number of days since epoch, but Excel files have different
         * epoch dates for different operating systems (see EpochDate for details).
         *
         * The decimal portion of the double represents the fraction of time into the day. It ranges from
         * 0 (midnight) to 0.99999999 (23:59:59 aka 11:59:59 PM). A decimal value of .5 represents noon.
         *
         * From Microsoft:
         *
         *     Excel stores dates as sequential numbers called serial values. On Windows, January 1, 1900 is serial
         *     number 1, and January 1, 2008 is serial number 39448 because it is 39,448 days after January 1, 1900.
         *
         * But that's not technically correct. For Windows systems, Excel has an off-by-one error due to incorrectly
         * treating 1900 as a leap year, causing the serial number to be shifted back by one. (Microsoft actually did
         * this intentionally to match the existing bug/behaviour in their competitor Lotus 1-2-3.)
         *
         * Also, dates in Windows systems are 1-index based, so to get t=0, you need to shift the serial number back
         * AGAIN by one.
         *
         * So on Windows machines, the double representing Jan 1st, 1900 is "2.0".
         *
         * Fortunately this horrible mess gets abstracted away from us by POI, so we don't need to worry about it here.
         */
        // TODO: getLocalDateTime is better but is only in POI 4.1.2+. The excel-import plugin (which is no longer
        //       supported) does not support that version so we're stuck with this until we can replace the plugin.
        boolean use1904Windowing = context?.epochDate == EpochDate.EXCEL_1904
        Date date = DateUtil.getJavaDate(excelDate, use1904Windowing, TimeZone.getTimeZone(zone), true)
        return asInstant(date)
    }

    /**
     * Null-safe conversion of a String to an Instant.
     *
     * This method should only need to be used in old flows that manually bind request params. Newer flows that
     * use request/command objects as controller method args will have their Instant fields automatically bound.
     *
     * @param date "2000-01-01T00:00Z", or "2000-01-01T00:00+05:00" for example
     * @param defaultZone The fallback timezone to default to if one is not provided in the date string.
     *                    This will typically be the user's timezone. If null, will fail to bind date strings
     *                    that don't have a timezone component.
     */
    static Instant asInstant(String date, ZoneId defaultZone=null, DateParserContext<Instant> context=null) {
        if (StringUtils.isBlank(date)) {
            return null
        }

        if (date.isDouble()) {
            return asInstant(Double.parseDouble(date), defaultZone, context)
        }

        try {
            return Instant.from(DataBindingConstants.DATE_TIME_ZONE_FORMAT.parse(date.trim()))
        } catch (DateTimeException e) {
            // We failed to bind the string to an Instant, likely because it was malformed. However only
            // throw the exception if we have no default timezone to try again with.
            if (!defaultZone) {
                throw e
            }
        }

        // If the given date is a valid local date (ie something like "2000-01-01" with no time or timezone) and we
        // have a default timezone that we can attach to it, then do so. We do this mainly to support pre-existing
        // logic. Our APIs previously accepted date-only strings, even for datetime fields (this is because we used
        // java.util.Date for both types of fields so didn't distinguish between them).

        // Very important note: In the previously mentioned scenario, pre-existing APIs that use java.util.Date fields
        // always default to server timezone when timezone is not provided in the date string. New APIs that use Instant
        // (and call this method) will now default to the user's timezone in that scenario (see InstantValueConverter).
        LocalDate localDate = LocalDateParser.asLocalDate(date)
        return asInstant(localDate, defaultZone)
    }

    /**
     * Null-safe conversion of a ZonedDateTime to an Instant.
     */
    static Instant asInstant(ZonedDateTime zonedDateTime) {
        return zonedDateTime?.toInstant()
    }

    /**
     * Null-safe conversion of a LocalDate to an Instant.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static Instant asInstant(LocalDate localDate, ZoneId zone) {
        if (!localDate) {
            return null
        }
        if (!zone) {
            throw new DateTimeException('Cannot convert a LocalDate to an Instant without specifying a timezone')
        }
        return asInstant(ZonedDateTimeParser.asZonedDateTime(localDate, zone))
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to an Instant.
     * Useful when working with old code that uses the old format.
     */
    static Instant asInstant(Date date) {
        return date?.toInstant()
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Calendar to an Instant.
     * Useful when working with old code that uses the old format.
     */
    static Instant asInstant(Calendar calendar) {
        return calendar?.toInstant()
    }
}
