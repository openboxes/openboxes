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
import java.time.ZoneId
import java.time.temporal.TemporalAccessor
import org.ocpsoft.prettytime.PrettyTime
import org.grails.plugins.web.taglib.FormatTagLib
import groovy.time.TimeDuration

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.date.DateDisplayFormat
import org.pih.warehouse.core.date.DateDisplayStyle
import org.pih.warehouse.core.date.DateFormatterContext
import org.pih.warehouse.core.date.DateFormatterManager

class DateTagLib {

    static namespace = "g"

    //@Cacheable("copyrightYear")
    def copyrightYear = { attrs, body ->
        out << (new Date().format(org.pih.warehouse.core.Constants.DEFAULT_YEAR_FORMAT))
    }

    /**
     * Formats a date for display and localizes it to a given locale and timezone.
     * Overrides the behaviour of Grails' g:formatDate, wrapping it with custom logic.
     *
     * @attr date Required. The date object to be formatted.
     * @attr locale Overrides the locale used for formatting.
     * @attr format Overrides the pattern that the date will be formatted to. Prefer using displayStyle when possible.
     * @attr timeZone Overrides the timezone used for formatting.
     * @attr displayStyle Which of the localizable patterns to use when formatting. Ignored if format is specified.
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
            default:
                throwTagError("Attribute [date] does not support type [${attrs.date.class}].")
        }
    }

    /**
     * Formats java.util.Date to a String for display in our GSPs.
     */
    private String formatJavaUtilDate(Object attrs) {
        FormatTagLib formatTagLib = grailsApplication.mainContext.getBean(
                'org.grails.plugins.web.taglib.FormatTagLib') as FormatTagLib

        if (!attrs.format) {
            attrs.format = Constants.DEFAULT_DATE_TIME_FORMAT
        }
        if (!attrs.timeZone && session.timezone) {
            attrs.timeZone = session.timezone
        }
        // See https://gsp.grails.org/latest/ref/Tags/formatDate.html for more information.
        return formatTagLib.formatDate.call(attrs)
    }

    /**
     * Formats java.time classes to a String for display in our GSPs.
     */
    private String formatJavaTimeDate(Object attrs) {
        DateFormatterManager dateFormatterManager =
                Holders.grailsApplication.mainContext.getBean("dateFormatterManager") as DateFormatterManager

        // We (intentionally) only support a subset of the configuration options that Grails' formatDate uses.
        // For a list of all options, see: https://gsp.grails.org/latest/ref/Tags/formatDate.html
        DateFormatterContext context = DateFormatterContext.builder()
                .withLocaleOverride(attrs.locale as Locale)
                // in Grails' formatDate, "format" means pattern... kinda confusing terminology.
                .withPatternOverride(attrs.format as String)
                .withTimezoneOverride(attrs.timeZone as ZoneId)
                // Note "displayStyle" differs from "style" from Grails' formatDate, though they function similarly.
                .withDisplayStyleOverride(attrs.displayStyle as DateDisplayStyle)
                // If either displayStyle or format are set, don't also set the display format or else it would
                // trigger errors in the DateFormatterContext.
                .withDisplayFormat(attrs.displayStyle || attrs.format ? null : DateDisplayFormat.GSP)
                .build()

        return dateFormatterManager.format(attrs.date, context)
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
