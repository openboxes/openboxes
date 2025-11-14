package org.pih.warehouse.databinding

import java.time.Instant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.session.SessionManager

/**
 * As of Java 8, Java.util.Date is functionally replaced with the java.time classes, but Grails 4 and older does not
 * support databinding a datetime String to an Instant (only timestamps) so we need to add support ourselves.
 * https://github.com/grails/grails-core/issues/11811
 */
@Component
class InstantValueConverter extends StringValueConverter<Instant> {

    @Autowired
    SessionManager sessionManager

    /**
     * Binds a given user-input String to an Instant.
     *
     * An Instant is an absolute moment in time, so to construct it we need a full datetime with timezone included.
     *
     * In order to make refactoring from java.util.Date to java.time.Instant a simpler process, we've opted to
     * default to midnight in the user's timezone if given a date-only string (such as "2000-01-01" or "01/01/2000").
     * Because we're not throwing an error if we're not given a timezone, we need to ensure that clients are super
     * clear on the behaviour. If they're assuming the date will be in server time, then we'll get date mismatch issues.
     *
     * @param value "2000-01-01T00:00Z", or "2000-01-01T00:00+05:00" for example
     */
    @Override
    Instant convertString(String value) {
        return DateUtil.asInstant(value, sessionManager.timezone?.toZoneId())
    }
}
