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

    def message = { attrs, body ->

        def defaultTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib')
        boolean databaseStoreEnabled = grailsApplication.config.openboxes.locale.custom.enabled

        // Checks the database to see if there's a localization property for the given code
        if (databaseStoreEnabled && session.user) {
            def localization = Localization.findByCodeAndLocale(attrs.code, session?.user?.locale?.toString())
            if (localization) {
                out << MessageFormat.format(localization.text, attrs?.args?.toArray()).encodeAsHTML()
                return
            }
        }

        // Display message normally
        else {
            Locale defaultLocale = new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
            attrs.locale = attrs.locale ?: session?.locale ?: session?.user?.locale ?: defaultLocale
            out << defaultTagLib.message.call(attrs)
        }
    }
}
