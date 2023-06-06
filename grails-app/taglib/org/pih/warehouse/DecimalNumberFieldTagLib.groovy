package org.pih.warehouse

import org.pih.warehouse.LocalizationUtil

import java.text.DecimalFormat

class DecimalNumberFieldTagLib extends FormatTagLib {

    static namespace = "g"

    def decimalNumberField = {attrs, body ->
        Locale locale = LocalizationUtil.localizationService.getCurrentLocale()
        DecimalFormat format = DecimalFormat.getInstance(locale)
        String decimalSeparator = format.decimalFormatSymbols.decimalSeparator

        // if attribute of initial value is passed
        // then transform it to the decimal format of current locale
        if (attrs.value) {
            attrs.value = (attrs.value as String).replace('.', decimalSeparator)
        }
        out << render(
                template: "/taglib/decimalFormatField",
                model: [attrs: attrs, decimalSeparator: decimalSeparator]
        )

    }
}
