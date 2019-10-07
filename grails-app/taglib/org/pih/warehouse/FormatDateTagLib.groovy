package org.pih.warehouse

import org.grails.plugins.web.taglib.FormatTagLib

class FormatDateTagLib extends FormatTagLib {

    static namespace = "g"

    Closure formatDate = { attrs, body ->
        FormatTagLib formatTagLib = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.FormatTagLib')
        if (session.timezone) {
            attrs.timeZone = session.timezone
        }
        out << formatTagLib.formatDate.call(attrs)
    }
}
