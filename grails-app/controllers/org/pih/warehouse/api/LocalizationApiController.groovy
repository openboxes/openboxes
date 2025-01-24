/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.Localization
import grails.core.GrailsApplication

class LocalizationApiController {

    def messageSource
    def localizationService
    GrailsApplication grailsApplication

    def list() {
        String languageCode = params.languageCode
        String prefix = params.prefix

        String[] supportedLocales = grailsApplication.config.openboxes.locale.supportedLocales

        Locale defaultLocale = Locale.default
        Locale currentLocale = localizationService.getCurrentLocale()
        Locale selectedLocale = languageCode ? LocalizationUtil.getLocale(languageCode) : currentLocale

        // Get the system default message properties
        Properties defaultMessageProperties = localizationService.getMessagesProperties(defaultLocale)

        // Get the country fallback language default message properties if it is country specific language
        // (Example: if we have es_MX we want to have messages fallback in this precedence es_MX -> es -> en)
        Properties fallbackMessageProperties = null
        if (selectedLocale.country && supportedLocales.contains(selectedLocale.language)) {
            Locale fallbackLocale = new Locale(selectedLocale.language)
            fallbackMessageProperties = localizationService.getMessagesProperties(fallbackLocale)
        }
        // Get the message properties for the selected locale
        Properties selectedMessageProperties = localizationService.getMessagesProperties(selectedLocale)

        // Get all translations for the given prefix and locale from the database
        List<Localization> localizedMessages = prefix ? Localization.findAllByCodeIlikeAndLocale("${prefix}%", selectedLocale.toString()) :
                Localization.findAllByLocale(selectedLocale.toString())
        Properties customMessageProperties = new Properties()
        localizedMessages.each { Localization localization ->
            customMessageProperties.put(localization.code, localization.text)
        }

        // Merge all messages from default, language fallback, selected, and custom message properties
        Properties mergedMessageProperties = new Properties()
        mergedMessageProperties.putAll(defaultMessageProperties)
        if (fallbackMessageProperties) {
            mergedMessageProperties.putAll(fallbackMessageProperties)
        }
        mergedMessageProperties.putAll(selectedMessageProperties)
        mergedMessageProperties.putAll(customMessageProperties)

        Properties messageProperties = prefix ? mergedMessageProperties.findAll {
            it.key.startsWith(prefix)
        } : mergedMessageProperties

        render([messages: messageProperties?.sort(), supportedLocales: supportedLocales, currentLocale: selectedLocale] as JSON)
    }

    def read() {
        String languageCode = params.lang
        Locale locale = localizationService.getLocale(languageCode)
        String message = messageSource.getMessage(params.id, params.list("args").toArray(), locale)
        render([code: params.id, message: message, currentLocale: locale] as JSON)
    }
}
