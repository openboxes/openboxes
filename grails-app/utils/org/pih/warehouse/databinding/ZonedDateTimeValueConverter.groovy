package org.pih.warehouse.databinding

import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * As of Java 8, Java.util.Date is functionally replaced with the java.time classes, but Grails 4 and older does not
 * support databinding a datetime String to an Instant (only timestamps) so we need to add support ourselves.
 * https://github.com/grails/grails-core/issues/11811
 */
@Component
class ZonedDateTimeValueConverter extends StringValueConverter<ZonedDateTime> {

    /**
     * Binds a given user-input String to a ZonedDateTime.
     *
     * A ZonedDateTime is an absolute moment in time in a specific locale, so to construct it we need a full datetime
     * with timezone included. As such, if the given string doesn't provide time or timezone information, we error.
     *
     * We could have been more forgiving and allow for time and time zone to be omitted (and default the resulting
     * ZonedDateTime to midnight server time), but we've encountered many timezone related issues in the past, so to
     * be safe  we require the client to always provide all the information required to build the ZonedDateTime.
     *
     * @param value "2000-01-01T00:00Z", or "2000-01-01T00:00+05:00" for example
     */
    @Override
    ZonedDateTime convertString(String value) {
        return StringUtils.isBlank(value) ?
                null :
                ZonedDateTime.from(DataBindingConstants.DATE_TIME_ZONE_FORMAT.parse(value.trim()))
    }
}
