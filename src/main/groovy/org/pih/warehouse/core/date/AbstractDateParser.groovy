package org.pih.warehouse.core.date

import java.time.ZoneId
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.core.session.SessionManager

/**
 * Deserializes input objects into valid date objects.
 *
 * For use only by data importers. Controllers should always use command objects containing date fields,
 * which can automatically handle data binding.
 */
abstract class AbstractDateParser<T> {

    @Autowired
    SessionManager sessionManager

    /**
     * Deserializes a date object into a date type. Assumes the date is in the user's timezone if the given date
     * does not have a timezone component.
     */
    abstract T parse(Object date, DateParserContext context=null)

    /**
     * @return ZoneId the timezone of the requesting user.
     */
    protected ZoneId getCurrentTimezone() {
        return sessionManager.timezone?.toZoneId()
    }
}
