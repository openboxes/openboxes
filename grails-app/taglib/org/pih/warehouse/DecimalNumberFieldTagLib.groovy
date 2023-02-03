package org.pih.warehouse

import org.codehaus.groovy.grails.plugins.web.taglib.FormatTagLib

class DecimalNumberFieldTagLib extends FormatTagLib {

    static namespace = "g"

    def decimalNumberField = {attrs, body ->
        attrs.locale = session?.locale ?: session?.user?.locale

        out << render(template: "../taglib/decimalFormatField", model: [attrs: attrs])

    }
}
