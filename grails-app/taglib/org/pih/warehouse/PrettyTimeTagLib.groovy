/**
 * Copyright (c) 2026 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 */
package org.pih.warehouse

import org.ocpsoft.prettytime.PrettyTime
import org.springframework.web.servlet.support.RequestContextUtils

/**
 * Minimal replacement for the grails-pretty-time plugin's <prettytime:display/>
 * tag (the plugin has no Grails 7 release). Formats a date relative to now,
 * e.g. "3 days ago", in the locale of the current request.
 *
 * Supported attrs (the subset OpenBoxes uses): date (required), showTime,
 * capitalize (default true, like the plugin).
 */
class PrettyTimeTagLib {

    static namespace = "prettytime"

    def display = { attrs ->
        Date date = attrs.date as Date
        if (!date) {
            return
        }

        Locale locale = RequestContextUtils.getLocale(request) ?: Locale.default
        String text = new PrettyTime(locale).format(date)

        boolean capitalize = attrs.containsKey('capitalize') ? attrs.capitalize.toString().toBoolean() : true
        if (capitalize && text) {
            text = text[0].toUpperCase() + text.substring(1)
        }

        boolean showTime = attrs.showTime.toString().toBoolean()
        if (showTime) {
            text += " (${g.formatDate(date: date, format: 'HH:mm')})"
        }

        out << text.encodeAsHTML()
    }
}
