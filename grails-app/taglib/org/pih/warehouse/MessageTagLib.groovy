/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse


import org.pih.warehouse.core.Localization

import java.text.MessageFormat


class MessageTagLib {

    static namespace = "warehouse"
    def grailsApplication
    def messageSource

    def message = { attrs, body ->

        def defaultTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib')

        boolean databaseStoreEnabled = grailsApplication.config.openboxes.locale.custom.enabled
        if (!databaseStoreEnabled) {
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            attrs.locale = attrs.locale ?: session?.user?.locale ?: session.locale ?: defaultLocale
            out << defaultTagLib.message.call(attrs)
            return
        }

        // Checks the database to see if there's a localization property for the given code
        if (session.user) {
            def localization = Localization.findByCodeAndLocale(attrs.code, session?.user?.locale?.toString())
            if (localization) {

                def message = localization.text

                // If there are arguments, we need to get the
                if (attrs?.args) {
                    message = messageSource.getMessage(attrs.code, null, attrs.default, session?.user?.locale)
                }

                if (session.useDebugLocale) {
                    def resolvedMessage = MessageFormat.format(localization.text, attrs?.args?.toArray())
                    out << """
								${resolvedMessage}
								<img class='open-localization-dialog'
									data-id="${localization.id}"
									data-code="${localization.code}" 
									data-locale="${localization.locale}" 
									data-message="${message}" 
									data-resolved-message="${resolvedMessage}" 
									data-message="${localization.text}" 
									data-args="${attrs.args}" 
									data-localized="" 
									src="${
                        createLinkTo(dir: 'images/icons/silk', file: 'database.png')
                    }"/>
							"""
                    return
                } else {
                    message = MessageFormat.format(localization.text, attrs?.args?.toArray())
                    out << """${message}"""
                }
                return
            }
        }

        // Display message in debug mode
        if (session.useDebugLocale) {
            def locales = grailsApplication.config.openboxes.locale.supportedLocales
            def localized = [:]
            def message = ""
            locales.each {
                def locale = new Locale(it)
                // This would be used if we actually wanted to translate the message
                def localizedMessage = messageSource.getMessage(attrs.code, attrs.args == null ? null : attrs.args.toArray(), attrs.default, locale)
                localized.put(it, localizedMessage)
            }
            def hasOthers = localized.values().findAll { word -> word != localized['en'] }

            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            attrs.locale = attrs.locale ?: session?.user?.locale ?: session.locale ?: defaultLocale

            def image = (!hasOthers) ? 'decline' : 'accept'

            message = messageSource.getMessage(attrs.code, null, attrs.default, request.locale)
            def resolvedMessage = "${defaultTagLib.message.call(attrs)}"
            out << """
					${resolvedMessage}
					<img class='open-localization-dialog'
						data-code="${attrs.code}" 
						data-locale="${attrs.locale}" 
						data-args="${attrs?.args?.join(',')}" 
						data-resolved-message="${resolvedMessage}" 
						data-message="${message}" 
						data-localized="${localized}" 
						src="${
                createLinkTo(dir: 'images/icons/silk', file: image + '.png')
            }" title="${attrs.code} = ${localized}"/>
					
				"""

        }
        // Display message normally
        else {
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            attrs.locale = attrs.locale ?: session?.user?.locale ?: session.locale ?: defaultLocale
            out << defaultTagLib.message.call(attrs)
        }
    }
}
