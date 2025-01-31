package org.pih.warehouse.databinding

import java.time.Instant
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

import org.pih.warehouse.DateUtil

/**
 * As of Java 8, Java.util.Date is functionally replaced with the java.time classes, but Grails 4 and older does not
 * support databinding a datetime String to an Instant (only timestamps) so we need to add support ourselves.
 * https://github.com/grails/grails-core/issues/11811
 */
@Component
class InstantValueConverter extends StringValueConverter<Instant> {

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
                Instant.from(DateUtil.FLEXIBLE_DATE_TIME_ZONE_FORMAT.parse(value.trim()))
    }
}
