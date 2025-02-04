package org.pih.warehouse.databinding

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * As of Java 8, Java.util.Date is functionally replaced with the java.time classes, but Grails 4 and older does not
 * support databinding a datetime String to an Instant (only timestamps) so we need to add support ourselves.
 * https://github.com/grails/grails-core/issues/11811
 */
@Component
class InstantValueConverter extends StringValueConverter<Instant> {

    private static final DateTimeFormatter FLEXIBLE_DATE_TIME_ZONE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern(DataBindingConstants.FLEXIBLE_DATE_TIME_ZONE_PATTERN)
            // Defaults to 00:00:00.000 if time is not provided in the String
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            // Defaults to UTC if timezone is not provided in the String
            .parseDefaulting(ChronoField.OFFSET_SECONDS, ZoneOffset.UTC.getTotalSeconds())
            .toFormatter()

    /**
     * Binds a given user-input String to an Instant.
     *
     * An Instant is an absolute moment in time, so to construct it we need a full datetime with timezone included.
     * As such, if the given string doesn't provide a time, we default to "00:00.000" (aka the start of the day),
     * and if it doesn't provide a timezone, we default to "Z" (aka UTC).
     *
     * We could have tried to determine the client's timezone by looking in the session and defaulting to that timezone
     * instead, but that would be making assumptions about what format the client is providing, which may not be
     * correct. The universally accepted "default" timezone is UTC, so for the sake of simplicity, we use that as a
     * fallback if the client doesn't provide timezone information themselves (though they really should).
     *
     * @param value "2000-01-01", "2000-01-01T00:00", "2000-01-01T00:00Z", "2000-01-01T00:00+05:00" for example
     */
    @Override
    Instant convertString(String value) {
        return StringUtils.isBlank(value) ?
                null :
                Instant.from(FLEXIBLE_DATE_TIME_ZONE_FORMAT.parse(value.trim()))
    }
}
