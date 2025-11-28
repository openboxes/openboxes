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
import org.apache.commons.lang.StringUtils
import org.grails.core.io.ResourceLocator
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.localization.LocaleManager
import org.pih.warehouse.core.localization.LocalizedMessageDto
import org.pih.warehouse.core.localization.LocalizedMessagesDto
import org.pih.warehouse.core.localization.MessageLocalizer

class LocalizationService {

    GrailsApplication grailsApplication
    ResourceLocator grailsResourceLocator
    LocaleManager localeManager
    MessageLocalizer messageLocalizer

    @Value('${openboxes.locale.custom.enabled}')
    boolean localizationDatabaseEnabled

    @Value('${openboxes.locale.supportedLocales}')
    String[] supportedLocales

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
     */
    Locale getLocale(String languageCode) {
        return StringUtils.isBlank(languageCode) ? currentLocale : localeManager.asLocale(languageCode)
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
     * Fetch all Properties from the messages.properties file associated with the locale.
     */
    private Properties getMessagesProperties(Locale locale) {
        Properties messagesProperties = new Properties()

        // File names are structured like: messages_<language-code>_<country-code>.properties
        // For example: messages.properties, messages_es.properties, messages_es_MX.properties
        String messagesPropertiesFilename =
                (locale == null || locale == localeManager.defaultLocale || locale.language == 'null') ?
                "messages.properties" :
                "messages_${locale.toString()}.properties"

        Resource resource = grailsResourceLocator.findResourceForURI('classpath:' + messagesPropertiesFilename)
        messagesProperties.load(resource.inputStream)

        return messagesProperties
    }

    /**
     * Fetch all localized messages for a given locale code and prefix.
     *
     * The fallback pattern for locales is region > language > default. For example: es_MX -> es -> default (en)
     * If a message does not exist in the requested code 'es_MX' but does exist in 'es', we add the message found
     * in 'es' to the returned results.
     *
     * @param localeCode The string representation of a locale. Ex: 'es_MX' or 'es'
     * @param prefix Filters the messages down to only those that start with the prefix. We prefix all frontend
     *               messages with 'react.' and so the frontend can filter for only those when querying for messages,
     *               leading to a smaller API response with backend-only messages being filtered out.
     */
    LocalizedMessagesDto list(String localeCode, String prefix) {
        Locale locale = getLocale(localeCode)

        // Get all messages for the requested locale along with with any overrides and fallbacks, then merge them all
        // into a single set of properties. Priority order is: database overrides > the given locale > fallback locales
        List<Properties> propertiesInPriorityOrder = getPropertiesWithFallbacksInPriorityOrder(locale, prefix)
        Properties mergedProperties = new Properties()
        for (Properties properties in propertiesInPriorityOrder) {
            properties.each { key, value -> mergedProperties.putIfAbsent(key, value) }
        }

        // String comparison is slow so we only filter for message prefix once we have the merged list.
        if (prefix) {
            mergedProperties = mergedProperties.findAll { (it.key as String).startsWith(prefix) } as Properties
        }

        return new LocalizedMessagesDto(
                messages: mergedProperties.sort() as Properties,
                supportedLocales: supportedLocales,
                currentLocale: locale,
        )
    }

    /**
     * Fetch a list of localization messages/properties for a given locale, along with any database overrides and
     * fallback messages. The list is ordered highest to lowest priority. If a message exists in a higher priority
     * properties file, we expect that to be used first.
     *
     * For example, if the locale is 'es_MX', translations in messages_es_MX.properties should take priority over
     * those in messages_es.properties, which take priority over those in messages.properties.
     */
    private List<Properties> getPropertiesWithFallbacksInPriorityOrder(Locale locale, String prefix) {
        List<Locale> fallbackLocales = getFallbackLocalesInPriorityOrder(locale)

        Map<Locale, Properties> localeProperties = getPropertiesForLocales(fallbackLocales + locale)

        // TODO: We should fetch the database overrides for the fallback locales as well so that we can default
        //       to those if a message is in the database for 'es' but not in the 'es_MX' messages.properties file.
        Properties messageOverrideProperties = getMessageOverridesAsProperties(locale, prefix)

        // Priority order is: database overrides > the given locale > fallback locales
        List<Properties> propertiesInPriorityOrder = []
        if (messageOverrideProperties) {
            propertiesInPriorityOrder.add(messageOverrideProperties)
        }
        propertiesInPriorityOrder.add(localeProperties.get(locale))
        propertiesInPriorityOrder.addAll(fallbackLocales.collect { localeProperties.get(it) })

        return propertiesInPriorityOrder
    }

    /**
     * We allow localization for individual codes to be overridden by data in the Localization database table.
     * These take priority over the messages in messages.properties files.
     */
    private List<Localization> getMessageOverrides(Locale locale, String prefix) {
        if (!localizationDatabaseEnabled) {
            return []
        }

        return prefix ?
                Localization.findAllByCodeIlikeAndLocale("${prefix}%", locale.toString()) :
                Localization.findAllByLocale(locale.toString())
    }

    private Properties getMessageOverridesAsProperties(Locale locale, String prefix) {
        List<Localization> localizedMessages = getMessageOverrides(locale, prefix)

        Properties customMessageProperties = new Properties()
        localizedMessages.each { Localization localization ->
            customMessageProperties.put(localization.code, localization.text)
        }
        return customMessageProperties
    }

    private Map<Locale, Properties> getPropertiesForLocales(List<Locale> locales) {
        Map<Locale, Properties> localeProperties = [:]
        for (Locale locale in locales) {
            Properties properties = getMessagesProperties(locale)
            if (properties) {
                localeProperties.put(locale, properties)
            }
        }
        return localeProperties
    }

    private List<Locale> getFallbackLocalesInPriorityOrder(Locale locale) {
        List<Locale> fallbackLocales = []

        // Region-specific locales fall back to language locales. Ex: 'es_MX' should fall back to 'es'
        if (locale.country && supportedLocales.contains(locale.language)) {
            Locale languageLocale = new Locale(locale.language)
            fallbackLocales.add(languageLocale)
        }

        // The default locale should always be a fallback option. This represents translations in messages.properties.
        fallbackLocales.add(localeManager.defaultLocale)

        return fallbackLocales
    }

    /**
     * Localizes a single message.
     *
     * Should only be needed for debugging purposes. To localize a message within the app, use MessageLocalizer.
     */
    LocalizedMessageDto localize(String messageCode, Object[] messageArgs, String languageCode) {
        Locale locale = getLocale(languageCode)
        String message = messageLocalizer.localize(messageCode, messageArgs, locale)
        return new LocalizedMessageDto(
                code: messageCode,
                message: message,
                currentLocale: locale,
        )
    }
}
