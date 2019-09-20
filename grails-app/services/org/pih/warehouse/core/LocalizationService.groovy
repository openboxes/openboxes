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

import grails.util.Metadata
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.pih.warehouse.util.LocalizationUtil
import org.springframework.core.io.ClassPathResource
import org.springframework.web.context.request.RequestContextHolder

class LocalizationService {

    // TODO: do we need to make this read-only?
    boolean transactional = false

    // session-scoped (because it needs access to the user)
    static scope = "session"

    // inject the grails application so we can access the default locale
    def grailsApplication


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
        return languageCode ? new Locale(languageCode) : currentLocale
    }

    /**
     * Gets the current locale or return default locale.
     */
    Locale getCurrentLocale() {
        // fetch the locale of the current user; if there isn't one, use the default locale
        return (RequestContextHolder.currentRequestAttributes().getSession().user?.locale ?:
                new Locale(grailsApplication.config.openboxes.locale.defaultLocale ?: "en"))
    }

    /**
     * Get all messages properties.
     *
     * @return
     */
    Properties getMessagesProperties(Locale locale) {
        Properties messagesProperties
        def messagesPropertiesFilename = (locale && locale.language != "en") ? "messages_${locale.language}.properties" : "messages.properties"

        // Get properties from classpath
        if (!Metadata.getCurrent().isWarDeployed()) {
            String messagesPropertiesUrl = "grails-app/i18n/" + messagesPropertiesFilename
            messagesProperties = getMessagesPropertiesFromClasspath(messagesPropertiesUrl)
        }
        // Get properties from exploded WAR file
        else {
            String messagesPropertiesUrl = "/WEB-INF/grails-app/i18n/" + messagesPropertiesFilename
            messagesProperties = getMessagesPropertiesFromResource(messagesPropertiesUrl)
        }
        return messagesProperties.sort()
    }


    /**
     * Get messages properties while running app using deployed WAR.
     *
     * @param messagesPropertiesUrl
     * @return
     */
    Properties getMessagesPropertiesFromResource(String messagesPropertiesUrl) {
        Properties properties = new Properties()
        def inputStream = ServletContextHolder.servletContext.getResourceAsStream(messagesPropertiesUrl)
        if (inputStream) {
            String messagesPropertiesString = IOUtils.toString(inputStream)
            properties.load(new StringReader(messagesPropertiesString))
            inputStream.close()
        }
        return properties
    }

    /**
     * Get messages properties while running app using grails run-app.
     *
     * @param messagesPropertiesUrl
     * @return
     */
    Properties getMessagesPropertiesFromClasspath(String messagesPropertiesUrl) {
        Properties properties = new Properties()
        File messagesPropertiesFile = new ClassPathResource(messagesPropertiesUrl)?.getFile()
        log.info "messagesPropertiesFile: ${messagesPropertiesFile}"
        if (messagesPropertiesFile.exists()) {
            InputStream messagesPropertiesStream = new FileInputStream(messagesPropertiesFile)
            properties.load(messagesPropertiesStream)
            messagesPropertiesStream.close()
        }
        return properties
    }

}
