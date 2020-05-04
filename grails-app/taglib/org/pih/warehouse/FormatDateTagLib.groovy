package org.pih.warehouse

import org.grails.plugins.web.taglib.FormatTagLib

class FormatDateTagLib {

    static namespace = "g"

    Closure formatDate = { attrs, body ->
        if (session.timezone) {
            attrs.timeZone = session.timezone
        }
        FormatTagLib formatTagLib = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.FormatTagLib')
        out << formatTagLib.formatDate.call(attrs)
    }
}
