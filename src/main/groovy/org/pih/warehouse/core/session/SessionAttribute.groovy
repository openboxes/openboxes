package org.pih.warehouse.core.session

import org.pih.warehouse.core.Location

/**
 * Enumerates the custom attributes that we put into the HttpSession.
 */
enum SessionAttribute {

    TIMEZONE('timezone', TimeZone),
    WAREHOUSE('warehouse', Location),
    IMPERSONATED_USER_ID('impersonatedUserId', String),
    ACTIVE_USER_ID('activeUserId', String)

    String attributeName
    Class type

    SessionAttribute(String attributeName, Class type) {
        this.attributeName = attributeName
        this.type = type
    }
}
