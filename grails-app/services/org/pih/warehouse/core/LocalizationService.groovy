/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.core.GrailsApplication
import org.grails.core.io.ResourceLocator

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.localization.LocaleManager

class LocalizationService {

    GrailsApplication grailsApplication
    ResourceLocator grailsResourceLocator
    LocaleManager localeManager

    String formatMetadata(Object object) {
        def format = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
        return format.metadata(obj: object)
    }

    String formatDate(Date date) {
        def format = grailsApplication.mainContext.getBean('org.pih.warehouse.FormatTagLib')
        return format.date(obj: date)
    }

    /**
     * Localizes the passed string value based on the current locale
     */
    String getLocalizedString(String value) {

        // null check
        if (!value) {
            return value
        }

        return LocalizationUtil.getLocalizedString(value, getCurrentLocale())
    }

    /**
     * Get a locale based on the given language code. Returns default if no language code is specified.
     *
     * @param languageCode
     * @return
     */
    Locale getLocale(String languageCode) {
        return languageCode ? localeManager.asLocale(languageCode) : currentLocale
    }

    /**
     * Fetch the locale of the user/request associated with the current thread.
     *
     * Falls back to the user's default locale if the session doesn't have a locale yet,
     * which in turn falls back to the global default locale if the user has no default configured.
     */
    Locale getCurrentLocale() {
        return localeManager.getCurrentLocale()
    }

    /**
     * Sets the locale of the user associated with the current request.
     *
     * @param localeCode Should be a valid ISO 3166 string with an optional region code (ex: "es", or "es-MX")
     * @param previousLocaleToUse Overrides the previous locale to set. If null, will use the session's current locale
     */
    Locale setLocale(String localeCode) {
        return localeManager.setCurrentLocale(localeCode)
    }

    /**
     * Put the user in localization mode by setting their locale to the special Crowdin pseudo-language.
     */
    void enableLocalizationMode() {
        localeManager.enableLocalizationMode()
    }

    /**
     * Disables localization mode, restoring the session to the locale that it was in before entering the mode.
     *
     * @param localeCodeToSwitchTo the locale to switch the user to after disabling the mode. If null, will use the
     *                             user's previous locale
     */
    void disableLocalizationMode(String localeCodeToSwitchTo) {
        localeManager.disableLocalizationMode(localeCodeToSwitchTo)
    }

    /**
     * Get all messages properties.
     *
     * @return
     */
    Properties getMessagesProperties(Locale locale) {
        Properties messagesProperties = new Properties()
        def messagesPropertiesFilename = (locale && locale.language != "en" && locale.language != 'null') ? "messages_${locale.toString()}.properties" : "messages.properties"

        def resource = grailsResourceLocator.findResourceForURI('classpath:' + messagesPropertiesFilename)
        messagesProperties.load(resource.inputStream)

        return messagesProperties
    }
}
