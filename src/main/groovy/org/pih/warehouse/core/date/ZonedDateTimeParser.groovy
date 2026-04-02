package org.pih.warehouse.core.date

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
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
    ZonedDateTime parseImpl(Object date, DateParserContext<ZonedDateTime> context=null) {
        switch (date) {
            case String:
                return asZonedDateTime(date as String, currentTimezone)
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

    static ZonedDateTime asZonedDateTime(String date, ZoneId defaultZone=null) {
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
