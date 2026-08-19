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
import org.springframework.core.io.Resource

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.localization.LocaleDeterminer

class LocalizationService {

    static final String MESSAGES_BUNDLE_BASENAME = "messages"

    GrailsApplication grailsApplication
    ResourceLocator grailsResourceLocator
    LocaleDeterminer localeDeterminer

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
        return languageCode ? localeDeterminer.asLocale(languageCode) : currentLocale
    }

    /**
     * Gets the current locale or return default locale.
     */
    Locale getCurrentLocale() {
        return localeDeterminer.getCurrentLocale()
    }

    /**
     * Get all messages properties for the given locale.
     *
     * @return the most specific message bundle that exists for the locale, or an empty Properties
     *         if not even the default bundle can be found
     */
    Properties getMessagesProperties(Locale locale) {
        Properties messagesProperties = new Properties()

        Resource resource = findMessagesResource(locale)
        if (!resource) {
            log.error "Unable to find any message bundle for locale ${locale}"
            return messagesProperties
        }

        resource.inputStream.withStream { InputStream inputStream ->
            messagesProperties.load(inputStream)
        }

        return messagesProperties
    }

    /**
     * Find the most specific message bundle that exists for the given locale.
     *
     * We can't assume that a bundle exists for every locale we're asked about. The locale can come from a request
     * param, from the user's profile or from the JVM default, none of which are restricted to the locales we ship
     * translations for, and even the supported locales don't all have a country specific bundle (we ship
     * messages_es_MX.properties but not, say, messages_fr_FR.properties). So instead of assuming, we walk from the
     * most specific candidate down to the default bundle (ex: fr_FR -> fr -> default).
     */
    private Resource findMessagesResource(Locale locale) {
        for (String filename : getMessagesFilenameCandidates(locale)) {
            Resource resource = grailsResourceLocator.findResourceForURI('classpath:' + filename)
            if (resource?.exists()) {
                return resource
            }
            log.debug "No message bundle ${filename} found for locale ${locale}, falling back to a less specific bundle"
        }
        return null
    }

    /**
     * Build the message bundle filenames to look for, ordered from the most to the least specific.
     */
    private static List<String> getMessagesFilenameCandidates(Locale locale) {
        List<String> suffixes = []

        // Guard against a locale that was parsed from the literal string "null" as well as against the empty locale.
        String language = locale?.language
        if (language && language != "null") {
            if (locale.country) {
                suffixes << "_${language}_${locale.country}"
            }
            suffixes << "_${language}"
        }

        // The default bundle is always the last resort.
        suffixes << ""

        return suffixes.collect { "${MESSAGES_BUNDLE_BASENAME}${it}.properties".toString() }
    }
}
