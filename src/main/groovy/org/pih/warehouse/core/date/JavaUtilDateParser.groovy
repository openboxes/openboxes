package org.pih.warehouse.core.date

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.DateUtil
import org.pih.warehouse.databinding.DataBindingConstants

/**
 * Deserializes input objects into Dates.
 *
 * For use only by data importers. APIs should always use command objects which automatically handle data binding.
 *
 * Designed to be temporary. We aim to eventually fully switch to java.time classes, which would deprecate this class.
 */
@Component
class JavaUtilDateParser extends AbstractDateParser<Date> {

    @Override
    protected Date parseImpl(Object date, DateParserContext<Date> context) {
        switch (date) {
            case String:
                return asDate(date as String)
            case Instant:
                return asDate(date as Instant)
            case ZonedDateTime:
                return asDate(date as ZonedDateTime)
            case LocalDate:
                return asDate(date as LocalDate)
            default:
                throw new UnsupportedOperationException("Cannot parse date of type [${date.class}]")
        }
    }

    /**
     * Binds a String to a (deprecated) java.util.Date. If no format is given, will use the default configured format,
     * which defaults the given date to midnight in the server timezone if that info is not provided in the date string.
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
                //.withZone(DateUtil.getSystemZoneOffset())
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
