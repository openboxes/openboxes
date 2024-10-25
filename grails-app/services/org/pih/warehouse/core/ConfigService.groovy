package org.pih.warehouse.core

import grails.core.GrailsApplication
import grails.util.Holders

/**
 * Handles fetching property values from the app configuration.
 */
class ConfigService {

    GrailsApplication grailsApplication

    /**
     * Fetches the given property, casting it to the given type.
     */
    public <T> T getProperty(String property, Class<T> type) {
        return grailsApplication.config.getProperty(property, type)
    }

    /**
     * Statically fetches the given property, casting it to the given type.
     * Only to be used outside of the context of the grails app. When possible, use the non-static version.
     */
    static <T> T getStaticProperty(String property, Class<T> type=String) {
        return Holders.config.getProperty(property, type)
    }

    /**
     * Fetches the given property as a string.
     *
     * Needed mainly because groovy classes have a default "getProperty(String)" method for fetching
     * properties of that class which we don't want getting called by accident so we override it.
     */
    String getProperty(String property) {
        return getProperty(property, String)
    }
}
