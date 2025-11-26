package org.pih.warehouse.core.session

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

/**
 * Enumerates the custom attributes that we put into the HttpSession.
 */
enum SessionAttribute {

    /**
     * The timezone of the user associated with the session
     */
    TIMEZONE('timezone', TimeZone),

    /**
     * The facility that user associated with the session has selected currently
     */
    WAREHOUSE('warehouse', Location),

    /**
     * The id of the User that the session is currently impersonating
     */
    IMPERSONATED_USER_ID('impersonatedUserId', String),

    /**
     * The id of the User that is currently active on the session.
     */
    ACTIVE_USER_ID('activeUserId', String),

    /**
     * The OpenBoxes user associated with the session
     */
    USER('user', User),

    /**
     * The current Locale of the session. Used for localization / translation.
     */
    LOCALE('locale', Locale),

    /**
     * The locale that the session was using previous to the current one.
     */
    PREVIOUS_LOCALE('previousLocale', Locale),

    /**
     * True if the current Locale of the session is the localizationModeLocale.
     */
    IS_IN_LOCALIZATION_MODE('useDebugLocale', Boolean)

    String attributeName
    Class type

    SessionAttribute(String attributeName, Class type) {
        this.attributeName = attributeName
        this.type = type
    }
}
