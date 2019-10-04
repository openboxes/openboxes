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
import grails.core.GrailsApplication

class LocalizationApiController {

    def messageSource
    def localizationService
    GrailsApplication grailsApplication

    def list = {
        String languageCode = params.lang
        String prefix = params.prefix

        Locale locale = languageCode ? localizationService.getLocale(languageCode) : localizationService.getCurrentLocale()
        Properties messagesProperties = localizationService.getMessagesProperties(locale)
        String[] supportedLocales = grailsApplication.config.openboxes.locale.supportedLocales

        def selectedMessages = prefix ? messagesProperties.findAll {
            it.key.startsWith(prefix)
        } : messagesProperties

        render([messages: selectedMessages?.sort(), supportedLocales: supportedLocales, currentLocale: locale] as JSON)
    }

    def read = {
        String languageCode = params.lang
        Locale locale = localizationService.getLocale(languageCode)
        String message = messageSource.getMessage(params.id, params.list("args").toArray(), locale)
        render([code: params.id, message: message, currentLocale: locale] as JSON)

    }

}
