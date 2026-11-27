package org.pih.warehouse.core.date

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.databinding.DataBindingConstants

/**
 * Deserializes input objects into ZonedDateTimes.
 *
 * For use only by data importers. APIs should always use command objects which automatically handle data binding.
 */
@Component
class ZonedDateTimeParser extends AbstractDateParser<ZonedDateTime> {

    @Override
    boolean isDefaultParserForType() {
        return true
    }

    @Override
    protected ZonedDateTime parseImpl(Object date, DateParserContext<ZonedDateTime> context) {
        switch (date) {
            case String:
                return asZonedDateTime(date as String, currentTimezone, context)
            case double:
            case Double:
            case BigDecimal:
                return asZonedDateTime(date as double, currentTimezone, context)
            case LocalDate:
                return asZonedDateTime(date as LocalDate, currentTimezone)
            case Instant:
                return asZonedDateTime(date as Instant, currentTimezone)
            case Calendar:
                return asZonedDateTime(date as Calendar, currentTimezone)
            case Date:
                return asZonedDateTime(date as Date, currentTimezone)
            default:
                throw new UnsupportedOperationException("Cannot parse date of type [${date.class}]")
        }
    }

    /**
     * Parse an Excel formatted date (a double representing the number of days since the Excel epoch) to a
     * ZonedDateTime in the given timezone.
     *
     * Excel dates have no timezone, so the date is interpreted in the given timezone.
     * See InstantParser.asInstant(Double...) for details on how Excel represents dates.
     */
    static ZonedDateTime asZonedDateTime(Double excelDate, ZoneId zone, DateParserContext context=null) {
        return asZonedDateTime(InstantParser.asInstant(excelDate, zone, context as DateParserContext<Instant>), zone)
    }

    static ZonedDateTime asZonedDateTime(String date, ZoneId defaultZone=null, DateParserContext context=null) {
        if (StringUtils.isBlank(date)) {
            return null
        }

        try {
            return ZonedDateTime.from(DataBindingConstants.DATE_TIME_ZONE_FORMAT.parse(date.trim()))
        } catch (DateTimeException e) {
            // We failed to bind the string to an ZonedDateTime, likely because it was malformed.
            // However only throw the exception if we have no default timezone to try again with.
            if (!defaultZone) {
                throw e
            }
        }

        // If given a date with a time but no zone (such as "2000-01-01 00:00:00"), we're forced to
        // make an assumption about what timezone the date is in. Because of this ambiguity, by default
        // we throw an error in this scenario, but if we opt to allow it (which we do during imports/exports
        // since Excel cannot store timezone), we parse the date into the given default zone.
        if (context?.allowDateTimesWithoutTimezone) {
            ZonedDateTime zonedDateTime = asZonedDateTimeWithoutTimezone(date, defaultZone)
            if (zonedDateTime) {
                return zonedDateTime
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
        return asZonedDateTime(localDate, defaultZone)
    }

    /**
     * Parses a String containing a date and time, but no timezone (ex: "2000-01-31 13:00:00") to a ZonedDateTime
     * in the given timezone.
     *
     * If the datetime doesn't exist in the timezone (because daylight savings skipped it), it is shifted forward by
     * the length of the gap. If it is ambiguous (because daylight savings repeated it), the earlier offset is used.
     *
     * @return the ZonedDateTime, or null if the String is not a datetime without a timezone.
     */
    static ZonedDateTime asZonedDateTimeWithoutTimezone(String date, ZoneId zone) {
        if (StringUtils.isBlank(date) || !zone) {
            return null
        }
        try {
            return LocalDateTime.parse(date.trim(), DataBindingConstants.DATE_TIME_FORMAT).atZone(zone)
        } catch (DateTimeException ignore) {
            return null
        }
    }

    static ZonedDateTime asZonedDateTime(Calendar date, ZoneId zone) {
        return asZonedDateTime(InstantParser.asInstant(date), zone)
    }

    /**
     * Null-safe conversion of an Instant to a ZonedDateTime. If no timezone is provided, we assume UTC.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static ZonedDateTime asZonedDateTime(Instant instant, ZoneId zone) {
        if (!instant) {
            return null
        }
        if (!zone) {
            throw new IllegalArgumentException('Cannot convert an Instant to a ZonedDateTime without specifying a timezone')
        }
        return instant.atZone(zone)
    }

    /**
     * Null-safe conversion of a (deprecated) java.util.Date to an ZonedDateTime. If no timezone is provided,
     * we assume UTC. Useful when working with old code that uses the old format.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static ZonedDateTime asZonedDateTime(Date date, ZoneId zone) {
        if (!date) {
            return null
        }
        if (!zone) {
            throw new IllegalArgumentException('Cannot convert a Date to a ZonedDateTime without specifying a timezone')
        }
        return InstantParser.asInstant(date).atZone(zone)
    }

    /**
     * Null-safe conversion of a LocalDate to a ZonedDateTime.
     *
     * @param zone "Z", or "UTC" or "+01:00" or "America/Anchorage" for example.
     */
    static ZonedDateTime asZonedDateTime(LocalDate localDate, ZoneId zone) {
        if (!localDate) {
            return null
        }
        if (!zone) {
            throw new IllegalArgumentException('Cannot convert a LocalDate to a ZonedDateTime without specifying a timezone')
        }
        return localDate.atStartOfDay(zone)
    }
}
