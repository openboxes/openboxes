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

import grails.core.GrailsApplication
import org.pih.warehouse.core.Localization
import org.pih.warehouse.LocalizationUtil

import java.text.MessageFormat

class MessageTagLib {

    static namespace = "warehouse"
    GrailsApplication grailsApplication

    def message = { attrs, body ->

        def defaultTagLib = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ValidationTagLib')

        boolean databaseStoreEnabled = grailsApplication.config.openboxes.locale.custom.enabled
        attrs.locale = attrs?.locale ?: LocalizationUtil.currentLocale  // set a locale if none is provided

        // Checks the database to see if there's a localization property for the given code
        if (databaseStoreEnabled && session.user) {
            Localization localization = Localization.findByCodeAndLocale(attrs.code, attrs.locale.toString())
            if (localization) {
                out << MessageFormat.format(localization.text, attrs?.args?.toArray()).encodeAsHTML()
                return
            }
        }

        out << defaultTagLib.message.call(attrs)
    }

    /**
     * Utility function which handles rendering messages to the user
     * available attributes:
     * * style - style of a message container ex: message, error (default: message)
     * * message - a single or a list of messages to be rendered in the template
     * by default takes the value stored in flash.message.
     * accepts data like: String, List<String>, Map<code: string, args: obj>, List<Map<code: string, args: obj>>
     */
    def renderMessage = { attrs ->
        def messages = attrs?.message ?: flash.message
        if (messages && !(messages instanceof List)) {
            messages = [messages]
        }
        out << render(
                template: "/taglib/renderMessage",
                model: [
                        style: attrs?.style ?: "message",
                        messages: messages,
                ]
        )
    }
}
