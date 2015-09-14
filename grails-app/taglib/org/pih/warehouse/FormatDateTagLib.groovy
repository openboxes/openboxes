package org.pih.warehouse

import org.codehaus.groovy.grails.plugins.web.taglib.FormatTagLib

class FormatDateTagLib extends FormatTagLib {

    static namespace = "g"

    def formatDate = { attrs, body ->
        FormatTagLib formatTagLib = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.FormatTagLib')
        if (session.timezone) {
            attrs.timeZone = session.timezone
        }
        out << formatTagLib.formatDate.call(attrs)
    }
}
