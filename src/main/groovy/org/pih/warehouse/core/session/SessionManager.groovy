package org.pih.warehouse.core.session

import javax.servlet.http.HttpSession
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.User

/**
 * Handles operations on a user's HTTP session.
 *
 * Note that the SessionManager is a simple wrapper on session attributes and so shouldn't contain any complex logic.
 * We can create specific components to handle more complex logic as needed (such as LocaleManager for Locale
 * attributes).
 *
 * Grails exposes the "session" field to controllers, views, and tag libs, so those classes can safely use that field.
 * This class is for accessing the session from anywhere else in the application.
 */
@Component
class SessionManager {

    /**
     * @return TimeZone The timezone of the user associated with the current request.
     */
    TimeZone getTimezone() {
        TimeZone timezone = getAttribute(SessionAttribute.TIMEZONE) as TimeZone

        // Default to the system timezone if the requesting user doesn't have one specified.
        return timezone ?: TimeZone.getTimeZone(DateUtil.getSystemZoneId().getId())
    }

    User getUser() {
        return getAttribute(SessionAttribute.USER) as User
    }

    void setUser(User user) {
        setAttribute(SessionAttribute.USER, user)
    }

    Locale getLocale() {
        return getAttribute(SessionAttribute.LOCALE) as Locale
    }

    void setLocale(Locale locale) {
        setAttribute(SessionAttribute.LOCALE, locale)
    }

    Locale getPreviousLocale() {
        return getAttribute(SessionAttribute.PREVIOUS_LOCALE) as Locale
    }

    void setPreviousLocale(Locale locale) {
        setAttribute(SessionAttribute.PREVIOUS_LOCALE, locale)
    }

    boolean isInLocalizationMode() {
        return (getAttribute(SessionAttribute.IS_IN_LOCALIZATION_MODE) as Boolean) ?: false
    }

    void setIsInLocalizationMode(boolean isInLocalizationMode) {
        setAttribute(SessionAttribute.IS_IN_LOCALIZATION_MODE, isInLocalizationMode)
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

    private Object getAttribute(SessionAttribute attribute) {
        return session.getAttribute(attribute.attributeName)
    }

    private void setAttribute(SessionAttribute attribute, Object value) {
        session.setAttribute(attribute.attributeName, value)
    }
}
