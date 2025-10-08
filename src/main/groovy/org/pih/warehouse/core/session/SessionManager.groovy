package org.pih.warehouse.core.session

import javax.servlet.http.HttpSession
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import org.pih.warehouse.DateUtil

/**
 * Handles operations on a user's HTTP session.
 *
 * Grails exposes the "session" field to controllers and tag libs, so those classes can safely use that field.
 * This class is for accessing the session from anywhere else in the application.
 */
@Component
class SessionManager {

    /**
     * @return TimeZone The timezone of the user associated with the current request.
     */
    TimeZone getTimezone() {
        TimeZone timezone = getAttribute(SessionAttribute.TIMEZONE, SessionAttribute.TIMEZONE.type)

        // Default to the system timezone if the requesting user doesn't have one specified.
        return timezone ?: TimeZone.getTimeZone(DateUtil.getSystemZoneId().getId())
    }

    /**
     * @return HttpSession Fetch the session of the user associated with the current request.
     */
    HttpSession getSession() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes()
        if (!requestAttributes instanceof ServletRequestAttributes) {
            throw new UnsupportedOperationException(
                    "Cannot get request attributes of type: ${requestAttributes?.getClass()}")
        }
        // Return without error if there's no active session, which can happen if we're not in the context
        // of an HTTP request. Ex: unit tests or running console commands
        return (requestAttributes as ServletRequestAttributes).request?.session
    }

    private <T> T getAttribute(SessionAttribute attribute, Class<T> type) {
        return type.cast(session.getAttribute(attribute.attributeName))
    }
}
