/**
 * Copyright (c) 2022 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON

import java.util.regex.Pattern

class HelpScoutApiController {

    def grailsApplication
    def localizationService

    /**
     * Return localized configuration, as JSON, for a HelpScout Beacon object.
     */
    def configuration = {
        Locale locale = session?.user?.locale ?: new Locale(grailsApplication.config.openboxes.locale.defaultLocale)
        String prefix = "helpscout.beacon"
        Pattern prefixPattern = Pattern.compile("^${prefix}\\.(\\w+)\\.label\$")

        def json = [
            "color"             : grailsApplication.config.openboxes.helpscout.widget.color,
            "enableFabAnimation": false,
            "labels"            : [
                /* do not promise response times while we adapt to HelpScout */
                "noTimeToWaitAround": null,
                "responseTime"      : null,
            ]
        ]

        localizationService.getMessagesPropertiesWithPrefix(prefix, locale)
            .each {
                json["labels"][prefixPattern.matcher(it.key).replaceFirst(/$1/)] = it.value
            }

        render json as JSON
    }
}
