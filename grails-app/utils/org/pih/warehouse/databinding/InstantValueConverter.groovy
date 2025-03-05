package org.pih.warehouse.databinding

import java.time.Instant
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

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
     * This defaulting to UTC is important. We don't want to rely on the system local time, which can vary from host
     * to host, and can change over time within a single host. Our app should behave the same way no matter where it is
     * hosted. We also don't want to make assumptions about the timezone that the end user is sending data in/from.
     *
     * The universally accepted "default" timezone is UTC, so for the sake of simplicity, we use that as a fallback if
     * the client doesn't provide timezone information. However, as much as possible we should insist that the client
     * DOES provide timezone information so that there is no ambiguity as to the precise datetime being provided.
     *
     * @param value "2000-01-01", "2000-01-01T00:00", "2000-01-01T00:00Z", "2000-01-01T00:00+05:00" for example
     */
    @Override
    Instant convertString(String value) {
        return StringUtils.isBlank(value) ?
                null :
                Instant.from(DataBindingConstants.FLEXIBLE_DATE_TIME_ZONE_FORMAT.parse(value.trim()))
    }
}
