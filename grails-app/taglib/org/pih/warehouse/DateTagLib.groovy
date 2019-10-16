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


import com.ocpsoft.pretty.time.PrettyTime
import grails.plugin.springcache.annotations.Cacheable
import groovy.time.TimeDuration
import org.pih.warehouse.core.Constants

class DateTagLib {

    static namespace = "g"

    @Cacheable("copyrightYear")
    def copyrightYear = { attrs, body ->
        out << (new Date().format(org.pih.warehouse.core.Constants.DEFAULT_YEAR_FORMAT))
    }

    def formatDate = { attrs, body ->
        def formatTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.FormatTagLib')

        if (!attrs.format) {
            attrs.format = Constants.DEFAULT_DATE_TIME_FORMAT
        }
        if (!attrs.timeZone && session.timezone) {
            attrs.timeZone = session.timezone
        }
        out << formatTagLib.formatDate.call(attrs)
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
