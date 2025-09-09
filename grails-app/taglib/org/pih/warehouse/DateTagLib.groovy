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

import grails.util.Holders
import java.time.temporal.TemporalAccessor
import org.ocpsoft.prettytime.PrettyTime
import groovy.time.TimeDuration
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.date.DateFormatterManager

class DateTagLib {

    static namespace = "g"

    //@Cacheable("copyrightYear")
    def copyrightYear = { attrs, body ->
        out << (new Date().format(org.pih.warehouse.core.Constants.DEFAULT_YEAR_FORMAT))
    }

    /**
     * Formats a date for display and localizes it to a given locale and timezone.
     */
    Closure formatDate = { attrs, body ->
        if (!attrs.containsKey('date')) {
            throwTagError("Attribute [date] is required.")
        }

        switch (attrs.date) {
            // All dates going forward should be java.time classes
            case TemporalAccessor:
                out << formatJavaTimeDate(attrs)
                return
            // Old dates are java.util.Date instances so we keep this for backwards compatability.
            case Date:
                out << formatJavaUtilDate(attrs)
                return
            // Make sure to handle optional date fields gracefully.
            case null:
                out << ''
                return
            default:
                throwTagError("Attribute [date] does not support type [${attrs.date.class}].")
        }
    }

    /**
     * Overrides Grails' built-in FormatTagLib, wrapping it with custom functionality.
     * See https://gsp.grails.org/latest/ref/Tags/formatDate.html for more information.
     */
    private String formatJavaUtilDate(Object attrs) {
        def formatTagLib = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.FormatTagLib')

        if (!attrs.format) {
            attrs.format = Constants.DEFAULT_DATE_TIME_FORMAT
        }
        if (!attrs.timeZone && session.timezone) {
            attrs.timeZone = session.timezone
        }
        return formatTagLib.formatDate.call(attrs)
    }

    private String formatJavaTimeDate(Object attrs) {
        DateFormatterManager dateFormatterManager =
                Holders.grailsApplication.mainContext.getBean("dateFormatterManager") as DateFormatterManager

        return dateFormatterManager.format(attrs.date)
    }

    def expirationDate = { attrs, body ->
        out << g.render(template: '/taglib/expirationDate', model: [attrs: attrs])
    }

    def relativeTime = { attrs, body ->
        TimeDuration timeDuration = attrs.timeDuration
        if (timeDuration) {
            if (timeDuration.years > 0) {
                out << "${timeDuration.years} years, ${timeDuration.days} days"
            } else if (timeDuration.days > 0) {
                out << "${timeDuration.days} days"
            } else if (timeDuration.hours > 0) {
                out << "${timeDuration.hours} hours"
            } else if (timeDuration.minutes > 0) {
                out << "${timeDuration.minutes} minutes"
            } else if (timeDuration.seconds > 0) {
                out << "${timeDuration.seconds} seconds"
            } else {
                out << "<span class='fade'>none</span>"
            }

        } else {
            out << "<span class='fade'>none</span>"
        }
    }

    def prettyDateFormat = { attrs, body ->
        String prettyDate = ""
        if (attrs.date) {
            def p = new PrettyTime()
            prettyDate = p.format(attrs.date)
        }
        out << prettyDate
    }
}
